/*************************************************************************************
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 *************************************************************************************/

package uk.ac.lancs.e_science.sakai.tools.blogger;

import uk.ac.lancs.e_science.jsf.components.blogger.IBloggerJSFEditionController;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import org.sakaiproject.util.ResourceLoader;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;
import javax.servlet.ServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane;
import org.sakaiproject.util.FormattedText;

import com.sun.faces.util.Util;

import uk.ac.lancs.e_science.sakai.tools.blogger.cacheForImages.CacheForImages;
import uk.ac.lancs.e_science.sakai.tools.blogger.util.JpegTransformer;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.SakaiProxy;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.LinkRule;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Paragraph;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.PostElement;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.State;

public class PostEditionAbstractController extends BloggerController implements IBloggerJSFEditionController
{

	protected Post post;

	protected Blogger blogger;

	protected String editedText = null;

	protected Image editedImage = null;

	protected String imageDescription = null;

	protected File editedFile = null;

	protected String fileDescription = null;

	protected String editedLinkExpression = null;

	protected String editedLinkDescription = null;

	protected int currentElementIndex = -1;

	protected String elementTypeUnderEdition = null;

	// flags
	protected boolean desactivateSetEditingText = false;

	protected boolean desactivateSetEditingLinkDecription = false;

	protected boolean desactivateSetEditingLinkExpression = false;

	protected boolean showModifyParagraphButton = false;

	protected boolean showModifyImageButton = false;

	protected boolean showModifyLinkButton = false;

	protected boolean showModifyFileButton = false;

	protected boolean isChanged = false;

	protected boolean treatingImageAsFile = false;

	// tabs
	protected static int INDEX_TEXT = 0;

	protected static int INDEX_IMG = 1;

	protected static int INDEX_LINK = 2;

	protected static int INDEX_FILE = 3;

	public String doSave()
	{
		blogger.storePost(post, SakaiProxy.getCurrentUserId(), SakaiProxy.getCurrentSiteId());
		resetFields();
		// we have to remove the images from cache
		if (post.getElements() != null)
		{

			for (int i = 0; i < post.getElements().length; i++)
			{
				if (post.getElements()[i] instanceof Image)
				{
					CacheForImages cache = CacheForImages.getInstance();
					cache.removeImage(((Image) post.getElements()[i]).getIdImage());
				}
			}
		}
		ValueBinding binding = Util.getValueBinding("#{postListViewerController}");
		PostListViewerController postListViewerController = (PostListViewerController) binding.getValue(FacesContext.getCurrentInstance());
		postListViewerController.reloadPosts();

		return postListViewerController.getLastView();

	}

	public String doPreview()
	{
		ServletRequest request = (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		request.setAttribute("post", post);

		return "previewPost";
	}

	public void setPost(Post post)
	{
		resetFields();
		deactivateModifiyButtons();
		isChanged = false;

		this.post = post;

	}

	public Post getPost()
	{
		return post;
	}

	// -----------------------------------------------------------------
	// ---------- KEYWORDS ---------------------------------------------
	// -----------------------------------------------------------------

	public String getKeywords()
	{
		StringBuilder sb = new StringBuilder("");

		if (post == null || post.getKeywords() == null || post.getKeywords().length == 0)
			return getKeywordsMessage();
		for (int i = 0; i < post.getKeywords().length; i++)
		{
			sb.append(post.getKeywords()[i]).append(", ");
		}
		return sb.toString().substring(0, sb.toString().lastIndexOf(", "));
	}

	public void setKeywords(String keywords)
	{
		if (keywords.trim().equals("") || (keywords.trim().equals(getKeywordsMessage())))
			post.setKeywords(null);
		else
		{
			post.setKeywords(null); // to start a new list of keywords
			StringTokenizer st = new StringTokenizer(keywords, ",");
			while (st.hasMoreTokens())
			{
				String token = st.nextToken();
				post.addKeyword(token);
			}
		}
	}

	public String getKeywordsMessage()
	{
		String keywordsMessage = null;

		ResourceLoader messages = new ResourceLoader("uk.ac.lancs.e_science.sakai.tools.blogger.bundle.Messages");

		String key = "keywords_instruction";

		try
		{
			keywordsMessage = messages.getString(key);
		}
		catch (MissingResourceException e)
		{
			keywordsMessage = "?? key '" + key + "' not found ??";
		}

		return keywordsMessage;
	}

	// -----------------------------------------------------------------
	// ---------- IMAGE ------------------------------------------------
	// -----------------------------------------------------------------
	public FileItem getImage()
	{
		return null;
	}

	public void setImage(FileItem fileItem)
	{
		treatingImageAsFile = false;
		if (fileItem != null && fileItem.get() != null && fileItem.get().length > 0)
		{
			byte[] content = fileItem.get();
			try
			{
				if (content != null)
				{
					if (content.length < 4 * 1024 * 1024)
					{ // TODO put that 4 Mb in a property
						editedImage = new Image(fileItem.getName(), content);
						imageDescription = fileItem.getName();
						if (imageDescription.indexOf(":\\") == 1) // we assume that is a windows file comming from ie
							imageDescription = imageDescription.substring(imageDescription.lastIndexOf("\\") + 1);
						JpegTransformer transformer = new JpegTransformer(content);
						editedImage.setDescription(imageDescription);
						editedImage.setImageContentWithThumbnailSize(transformer.transformJpegFixingLongestDimension(125, 0.8f));
						editedImage.setImageContentWithWebSize(transformer.transformJpegFixingLongestDimension(300, 0.8f));
					}
					else
					{
						editedImage = null;
						imageDescription = "";
						setFile(fileItem.getName(), content);
						treatingImageAsFile = true;
					}
				}
			}
			catch (Exception e)
			{
				// if we can not treat the Image because of the format, it will be treat as a file
				editedImage = null;
				imageDescription = "";
				setFile(fileItem.getName(), content);
				treatingImageAsFile = true;
			}
		}
	}

	public String addImage()
	{
		if (treatingImageAsFile)
		{
			addFile();
			treatingImageAsFile = false;
		}
		else
		{
			resetCurrentElementIndex();
			post.addElement(editedImage);
			CacheForImages imageCache = CacheForImages.getInstance();
			imageCache.addImage(editedImage);
			deactivateModifiyButtons();
			resetFields();
			isChanged = true;
		}
		return "";
	}

	public String modifyImage()
	{
		CacheForImages imageCache = CacheForImages.getInstance();
		if (editedImage != null)
		{
			imageCache.removeImage(((Image) post.getElements()[currentElementIndex]).getIdImage());
			post.replaceElement(editedImage, currentElementIndex);
			imageCache.addImage(editedImage);
			editedImage = null;
		}
		deactivateModifiyButtons();
		isChanged = true;
		return "";
	}

	public String getImageDescription()
	{
		return imageDescription;
	}

	// -----------------------------------------------------------------
	// ---------- FILE -------------------------------------------------
	// -----------------------------------------------------------------

	public FileItem getFile()
	{
		return null;
	}

	public void setFile(FileItem i)
	{
		if (i != null && i.get() != null && i.get().length > 0)
		{
			try
			{
				byte[] content = i.get();
				String name = i.getName();
				if (name.indexOf(":\\") == 1) // we assume that is a windows file comming from ie
					name = name.substring(name.lastIndexOf("\\") + 1);
				setFile(name, content);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void setFile(String fileName, byte[] content)
	{
		if (content != null)
		{
			editedFile = new File(fileName, content);
		}
		fileDescription = fileName;
		editedFile.setDescription(fileDescription);
	}

	public String addFile()
	{
		resetCurrentElementIndex();
		post.addElement(editedFile);
		deactivateModifiyButtons();
		resetFields();
		isChanged = true;
		return "";
	}

	public String modifyFile()
	{
		if (editedFile != null)
		{
			post.replaceElement(editedFile, currentElementIndex);
			editedFile = null;
		}
		deactivateModifiyButtons();
		isChanged = true;
		return "";
	}

	public String getFileDescription()
	{
		return fileDescription;
	}

	// -----------------------------------------------------------------
	// ---------- LINK -------------------------------------------------
	// -----------------------------------------------------------------

	public String addLink()
	{
		HtmlPanelTabbedPane panel = (HtmlPanelTabbedPane) FacesContext.getCurrentInstance().getViewRoot().findComponent("PostForm:tabbedPane");
		panel.setSelectedIndex(INDEX_LINK);

		resetCurrentElementIndex();
		if (editedLinkDescription != null && editedLinkExpression != null)
		{
			post.addElement(new LinkRule(editedLinkDescription, editedLinkExpression));
			editedLinkDescription = null;
			editedLinkExpression = null;
		}
		deactivateModifiyButtons();
		isChanged = true;
		return "";
	}

	public String modifyLink()
	{
		if (editedLinkDescription != null && editedLinkExpression != null)
		{
			post.replaceElement(new LinkRule(editedLinkDescription, editedLinkExpression), currentElementIndex);
			editedLinkDescription = null;
			editedLinkExpression = null;
		}
		isChanged = true;
		deactivateModifiyButtons();
		return "";
	}

	public String getLinkDescription()
	{
		return editedLinkDescription;
	}

	public void setLinkDescription(String linkDescription)
	{
		if (desactivateSetEditingLinkDecription)
		{
			desactivateSetEditingLinkDecription = false;
			return;
		}
		editedLinkDescription = linkDescription;
	}

	public String getLinkExpression()
	{
		return editedLinkExpression;
	}

	public void setLinkExpression(String linkExpression)
	{
		if (desactivateSetEditingLinkExpression)
		{
			desactivateSetEditingLinkExpression = false;
			return;
		}
		editedLinkExpression = linkExpression;
	}

	// -----------------------------------------------------------------
	// ---------- SHORT TEXT OR ABSTRACT--------------------------------
	// -----------------------------------------------------------------

	public void setShortText(String editingText)
	{
		StringBuilder errorMessages = new StringBuilder();
		editingText = FormattedText.processFormattedText(editingText, errorMessages, true, false);
		post.setShortText(editingText);
	}

	public String getShortText()
	{
		return post.getShortText();
	}

	// -----------------------------------------------------------------
	// ---------- PARAGRAPH -----------------------------------------
	// -----------------------------------------------------------------

	public String addParagraph()
	{
		resetCurrentElementIndex();
		if (editedText != null && !editedText.trim().equals(""))
		{
			StringBuilder errorMessages = new StringBuilder();
			editedText = FormattedText.processFormattedText(editedText, errorMessages, true, false);
			post.addElement(new Paragraph(editedText));
		}

		editedText = "";
		deactivateModifiyButtons();
		isChanged = true;
		return "";
	}

	public String modifyParagraph()
	{
		if (editedText != null && !editedText.trim().equals(""))
		{
			StringBuilder errorMessages = new StringBuilder();
			editedText = FormattedText.processFormattedText(editedText, errorMessages, true, false);
			post.replaceElement(new Paragraph(editedText), currentElementIndex);
		}

		editedText = "";
		deactivateModifiyButtons();
		isChanged = true;
		return "";
	}

	public void setEditingText(String editingText)
	{
		if (desactivateSetEditingText)
			desactivateSetEditingText = false;
		else
		{
			this.editedText = editingText.trim();
		}
	}

	public String getEditingText()
	{
		return editedText;
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	public String setCurrentElementIndex(int currentElementIndex)
	{
		HtmlPanelTabbedPane panel = (HtmlPanelTabbedPane) FacesContext.getCurrentInstance().getViewRoot().findComponent("PostForm:tabbedPane");
		desactivateSetEditingText = true;
		desactivateSetEditingLinkDecription = true;
		desactivateSetEditingLinkExpression = true;
		this.currentElementIndex = currentElementIndex;
		if (currentElementIndex >= 0)
		{
			deactivateModifiyButtons();
			PostElement element = post.getElements()[currentElementIndex];
			if (element instanceof Paragraph)
			{
				editedText = ((Paragraph) element).getText();
				showModifyParagraphButton = true;
				editedImage = null;
				panel.setSelectedIndex(INDEX_TEXT);
			}
			if (element instanceof Image)
			{
				editedImage = (Image) element;
				imageDescription = editedImage.getDescription();
				showModifyImageButton = true;
				editedText = "";
				panel.setSelectedIndex(INDEX_IMG);
			}
			if (element instanceof LinkRule)
			{
				editedLinkDescription = ((LinkRule) element).getDescription();
				editedLinkExpression = ((LinkRule) element).getLinkExpression();
				showModifyLinkButton = true;
				editedText = "";
				panel.setSelectedIndex(INDEX_LINK);

			}
			if (element instanceof File)
			{
				editedFile = (File) element;
				fileDescription = editedFile.getDescription();
				showModifyFileButton = true;
				editedText = "";
				panel.setSelectedIndex(INDEX_FILE);
			}
		}
		return "";

	}

	public void removeElement(int index)
	{

		PostElement element = post.getElements()[index];
		if (element instanceof Image)
		{
			CacheForImages cache = CacheForImages.getInstance();
			cache.removeImage(((Image) element).getIdImage());
		}
		post.removeElement(index);
		deactivateModifiyButtons();
		isChanged = true;
	}

	public int getCurrentElementIndex()
	{
		return currentElementIndex;
	}

	public boolean getShowModifyParagraphButton()
	{
		return showModifyParagraphButton;
	}

	public boolean getShowAddParagraphButton()
	{
		return !showModifyParagraphButton;
	}

	public boolean getShowModifyImageButton()
	{
		return showModifyImageButton;
	}

	public boolean getShowModifyLinkButton()
	{
		return showModifyLinkButton;
	}

	public boolean getShowModifyFileButton()
	{
		return showModifyFileButton;
	}

	public void setElementTypeUnderEdition(String typeName)
	{
		elementTypeUnderEdition = typeName;
	}

	public void upElement(int index)
	{
		if (index == 0)
			return;
		PostElement e1 = post.getElements()[index - 1];
		PostElement e2 = post.getElements()[index];
		post.replaceElement(e1, index);
		post.replaceElement(e2, index - 1);
		deactivateModifiyButtons();
		isChanged = true;

	}

	public void downElement(int index)
	{
		if (index == post.getElements().length)
			return;
		PostElement e1 = post.getElements()[index];
		PostElement e2 = post.getElements()[index + 1];
		post.replaceElement(e1, index + 1);
		post.replaceElement(e2, index);
		deactivateModifiyButtons();
		isChanged = true;

	}

	public void setActivePane(int activePane)
	{

	}

	public int getActivePane()
	{
		return 0;
	}

	private void resetCurrentElementIndex()
	{
		this.currentElementIndex = -1;
	}

	protected void resetFields()
	{
		editedText = "";
		editedImage = null;
		editedLinkDescription = null;
		editedLinkExpression = null;
		editedFile = null;
		fileDescription = null;
	}

	private void deactivateModifiyButtons()
	{
		showModifyImageButton = false;
		showModifyParagraphButton = false;
		showModifyLinkButton = false;
		showModifyFileButton = false;
	}

	public List getVisibilityList()
	{

		ArrayList result = new ArrayList();
		result.add(new SelectItem(new Integer(State.PRIVATE), "PRIVATE"));
		result.add(new SelectItem(new Integer(State.TUTOR), "TUTOR"));
		result.add(new SelectItem(new Integer(State.SITE), "SITE"));
		// result.add(new SelectItem(new Integer(State.PUBLIC),"PUBLIC"));
		return result;
	}

	public boolean getIsChanged()
	{
		return isChanged; // if this value is true, that means we can be modifiying a paragraph
	}

}
