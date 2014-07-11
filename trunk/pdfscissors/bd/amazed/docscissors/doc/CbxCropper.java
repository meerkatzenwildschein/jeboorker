package bd.amazed.docscissors.doc;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ProgressMonitor;

import org.apache.commons.io.FilenameUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.compression.truezip.TrueZipUtils;
import org.rr.jeborker.metadata.MetadataUtils;
import org.rr.jeborker.metadata.comicbook.ArchiveHandlerFactory;
import org.rr.jeborker.metadata.comicbook.ComicBookDocument;
import org.rr.jeborker.metadata.comicbook.ComicBookPageInfo;
import org.rr.jeborker.metadata.comicbook.ComicBookReader;
import org.rr.jeborker.metadata.comicbook.IArchiveHandler;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageInfo;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

import bd.amazed.docscissors.model.PageRectsMap;

import com.itextpdf.text.DocumentException;


/**
 * Cropper for CBZ and CBR
 */
public class CbxCropper extends DocumentCropper {

	public CbxCropper(IResourceHandler file) {
		super(file);
	}

	private static Rectangle getMaxBoundingBox(ComicBookReader reader) throws IOException {
		Rectangle maxBoundingBox = new Rectangle(0, 0, 0, 0);
		List<ComicBookPageInfo> pages = reader.getDocument().getPages();
		if(pages.isEmpty()) {
			IArchiveHandler archiveHandler = reader.getArchiveHandler();
			List<String> archiveEntries = archiveHandler.getArchiveEntries();
			for (String archiveEntry : archiveEntries) {
				byte[] imageBytes = archiveHandler.getArchiveEntry(archiveEntry);
				IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getVirtualResourceHandler(archiveEntry, imageBytes));
				ImageInfo imageInfo = imageProvider.getImageInfo();

				maxBoundingBox.width = Math.max(imageInfo.getWidth(), maxBoundingBox.width);
				maxBoundingBox.height = Math.max(imageInfo.getHeight(), maxBoundingBox.height);
			}
		} else {
			for (ComicBookPageInfo page : pages) {
				int height = page.getImageHeight();
				int width = page.getImageWidth();

				maxBoundingBox.width = Math.max(width, maxBoundingBox.width);
				maxBoundingBox.height = Math.max(height, maxBoundingBox.height);
			}
		}
		return maxBoundingBox;
	}

	@Override
	public void cancel() {
		isCancel = true;
	}

	@Override
	public void crop(DocumentInfo docInfo, File targetFile, PageRectsMap pageRectsMap, int viewWidth, int viewHeight, ProgressMonitor progressMonitor)
			throws IOException, DocumentException {
		IResourceHandler targetResourceHandler = ResourceHandlerFactory.getResourceHandler(targetFile);
		IArchiveHandler targetArchiveHandler = ArchiveHandlerFactory.getHandler(targetResourceHandler);

		ComicBookReader reader = new ComicBookReader(docInfo.getOriginalFile());
		ComicBookDocument doc = reader.getDocument();
		List<String> imageNames = doc.getImageNames();
		Set<String> usedImageNames = new HashSet<String>();

		for(int i = 0; i < imageNames.size(); i++) {
			String imageName = imageNames.get(i);
			byte[] imageBytes = reader.getArchiveHandler().getArchiveEntry(imageName);
			IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getVirtualResourceHandler(imageName, imageBytes));
			BufferedImage image = imageProvider.getImage();

			pageRectsMap.setOriantationMode(PageRectsMap.ORIENTATION_Y_TOP);
			ArrayList<Rectangle> cropRectsInIPDFCoords = pageRectsMap.getConvertedRectsForCropping(i + 1, viewWidth, viewHeight, docInfo.getNormalizedWidth(), docInfo.getNormalizedHeight());
			addImageNameWhenUsed(usedImageNames, imageName, cropRectsInIPDFCoords);
			for(Rectangle cropRectsInIPDFCoord : cropRectsInIPDFCoords) {
				BufferedImage copedImage = ImageUtils.crop(image, cropRectsInIPDFCoord, Color.WHITE);
				byte[] expandedImageBytes = ImageUtils.getImageBytes(copedImage, imageProvider.getImageInfo().getMimeType());
				String targetImageName = imageName;
				for(int j = 0; usedImageNames.contains(targetImageName); j++) {
					targetImageName = FilenameUtils.removeExtension(imageName) + "_" + j + "." + FilenameUtils.getExtension(imageName);
				}
				usedImageNames.add(targetImageName);
				targetArchiveHandler.addArchiveEntry(targetImageName, expandedImageBytes);
			}
		}

		TrueZipUtils.unmout();

		MetadataUtils.copyMetadata(docInfo.getOriginalFile(), targetResourceHandler);
	}

	private void addImageNameWhenUsed(Set<String> usedImageNames, String imageName, ArrayList<Rectangle> cropRectsInIDocCoords) {
		boolean multiplePages = cropRectsInIDocCoords.size() > 1;
		if(multiplePages) {
			usedImageNames.add(imageName); //mark the current name as used when having multiple rectangles on the page
		}
	}

	@Override
	public DocumentInfo getDocumentInfo() throws DocumentException, IOException {
		ComicBookReader reader = new ComicBookReader(mainFile);
		ComicBookDocument doc = reader.getDocument();
		Rectangle maxBoundingBox = getMaxBoundingBox(reader);

		DocumentInfo docFile = new DocumentInfo(mainFile, mainFile, doc.getCount());
		docFile.setDocInfo(doc.getInfo());
		docFile.setPageCount(doc.getCount());
		docFile.setNormalizedWidth(Math.abs((float)maxBoundingBox.getWidth()));
		docFile.setNormalizedHeight(Math.abs((float)maxBoundingBox.getHeight()));

		return docFile;
	}

}
