/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral.impl.fedora;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.guanxi.common.GuanxiException;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Adapted from:
 * http://svn.apache.org/viewvc/jakarta/httpcomponents/oac.hc3x/trunk/src/contrib/org/apache/commons/httpclient/contrib/ssl/AuthSSLProtocolSocketFactory.java?view=markup
 * http://jakarta.apache.org/httpcomponents/httpclient-3.x/sslguide.html
 */
public class FedoraProtocolSocketFactory implements ProtocolSocketFactory {
  private String keystorePath = null;
  private String keystorePassword = null;
  private String truststorePath = null;
  private String truststorePassword = null;
  private SSLContext sslcontext = null;

  /**
   * Constructor for AuthSSLProtocolSocketFactory. Either a keystore or truststore file
   * must be given. Otherwise SSL context initialization error will result.
   *
   * @param keystorePath Full name/path of the keystore file. May be <tt>null</tt> if HTTPS client
   *        authentication is not to be used.
   * @param keystorePassword Password to unlock the keystore. IMPORTANT: this implementation
   *        assumes that the same password is used to protect the key and the keystore itself.
   * @param truststorePath Full path/name of the truststore file. May be <tt>null</tt> if HTTPS server
   *        authentication is not to be used.
   * @param truststorePassword Password to unlock the truststore.
   */
  public FedoraProtocolSocketFactory(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword) {
    super();
    this.keystorePath = keystorePath;
    this.keystorePassword = keystorePassword;
    this.truststorePath = truststorePath;
    this.truststorePassword = truststorePassword;
  }

  private static KeyStore createKeyStore(String keystoreFile, String keystorePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    KeyStore ks = KeyStore.getInstance("JKS");

    // Does the keystore exist?
    File keyStore = new File(keystoreFile);
    if (keyStore.exists())
      ks.load(new FileInputStream(keystoreFile), keystorePassword.toCharArray());
    else
      ks.load(null, null);

    return ks;
  }

  private static KeyManager[] createKeyManagers(KeyStore keystore, String password)
      throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
    KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmfactory.init(keystore, password != null ? password.toCharArray(): null);
    return kmfactory.getKeyManagers();
  }

  /**
   * For this to work, the truststore must be populated with server certificates
   *
   * @param truststorePath
   * @param truststorePassword
   * @return
   * @throws KeyStoreException
   * @throws NoSuchAlgorithmException
   */
  private static TrustManager[] createTrustManagers(String truststorePath, String truststorePassword) throws KeyStoreException, NoSuchAlgorithmException {
    try {
      // First, get the default TrustManagerFactory.
      TrustManagerFactory tmFact = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

      // Next, set up the TrustStore to use. We need to load the file into
      // a KeyStore instance.
      FileInputStream fis = new FileInputStream(truststorePath);
      KeyStore ks = KeyStore.getInstance("jks");
      ks.load(fis, truststorePassword.toCharArray());
      fis.close();

      // Now we initialise the TrustManagerFactory with this KeyStore
      tmFact.init(ks);

      // And now get the TrustManagers
      return tmFact.getTrustManagers();
    }
    catch(IOException ioe) {
      return null;
    }
    catch(CertificateException ce) {
      return null;
    }
  }

  private SSLContext createSSLContext() throws GuanxiException {
    try {
      KeyManager[] keymanagers = null;
      TrustManager[] trustmanagers = null;

      if (keystorePath != null) {
        KeyStore keystore = createKeyStore(keystorePath, keystorePassword);
        keymanagers = createKeyManagers(keystore, keystorePassword);
      }

      if (truststorePath != null) {
        createKeyStore(truststorePath, truststorePassword);
        trustmanagers = createTrustManagers(truststorePath, truststorePassword);
      }

      SSLContext sslcontext = SSLContext.getInstance("SSL");
      sslcontext.init(keymanagers, trustmanagers, null);

      return sslcontext;

    }
    catch (NoSuchAlgorithmException e) {
      throw new GuanxiException("Unsupported algorithm exception: " + e.getMessage());
    }
    catch (KeyStoreException e) {
      throw new GuanxiException("Keystore exception: " + e.getMessage());
    }
    catch (GeneralSecurityException e) {
      throw new GuanxiException("Key management exception: " + e.getMessage());
    }
    catch (IOException e) {
      throw new GuanxiException("I/O error reading keystore/truststore file: " + e.getMessage());
    }
  }

  private SSLContext getSSLContext() {
    if (sslcontext == null) {
      try {
        sslcontext = createSSLContext();
      }
      catch(GuanxiException ge) {
        return null;
      }
    }

    return sslcontext;
  }

  /**
   * Attempts to get a new socket connection to the given host within the given time limit.
   * <p>
   * To circumvent the limitations of older JREs that do not support connect timeout a
   * controller thread is executed. The controller thread attempts to create a new socket
   * within the given limit of time. If socket constructor does not return until the
   * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
   * </p>
   *
   * @param host the host name/IP
   * @param port the port on the host
   * @param localAddress the local host name/IP to bind the socket to
   * @param localPort the port on the local machine
   * @param params {@link HttpConnectionParams Http connection parameters}
   *
   * @return Socket a new socket
   *
   * @throws IOException if an I/O error occurs while creating the socket
   */
  public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException {
    int timeout = params.getConnectionTimeout();
    SocketFactory socketfactory = getSSLContext().getSocketFactory();

    if (timeout == 0) {
      return socketfactory.createSocket(host, port, localAddress, localPort);
    }
    else {
      Socket socket = socketfactory.createSocket();
      SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
      SocketAddress remoteaddr = new InetSocketAddress(host, port);
      socket.bind(localaddr);
      socket.connect(remoteaddr, timeout);
      return socket;
    }
  }

  /**
   * @see org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
   */
  public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
    return getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
  }

  /**
   * @see org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory#createSocket(java.lang.String,int)
   */
  public Socket createSocket(String host, int port) throws IOException {
    return getSSLContext().getSocketFactory().createSocket(host, port);
  }

  /**
   * @see org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
   */
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
    return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
  }
}