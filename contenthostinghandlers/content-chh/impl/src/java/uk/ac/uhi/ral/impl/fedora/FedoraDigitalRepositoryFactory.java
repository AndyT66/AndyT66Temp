/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral.impl.fedora;

import uk.ac.uhi.ral.DigitalRepositoryFactory;
import uk.ac.uhi.ral.DigitalRepository;

public class FedoraDigitalRepositoryFactory implements DigitalRepositoryFactory {
  private String keystorePath = null;
  private String keystorePassword = null;
  private String truststorePath = null;
  private String truststorePassword = null;
  private boolean debug = false;
  private String debugOutputPath = null;

  public DigitalRepository create() {
    return new FedoraDigitalRepositoryImpl(keystorePath, keystorePassword, truststorePath, truststorePassword,
                                           debug, debugOutputPath);
  }

  // Spring injection
  public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
  public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
  public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }
  public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }
  public void setDebug(boolean debug) { this.debug = debug; }
  public void setDebugOutputPath(String debugOutputPath) { this.debugOutputPath = debugOutputPath; }
  public String getDebugOutputPath() { return debugOutputPath; }
}
