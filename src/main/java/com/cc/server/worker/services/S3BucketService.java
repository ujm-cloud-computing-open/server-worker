package com.cc.server.worker.services;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

@Service
public class S3BucketService {
//	    @Autowired
//	    AmazonS3Client amazonS3Client;
	    @Autowired
	    AmazonS3 amazonS3Client; 
	    @Autowired
	    ImageProcessing imageService;
	    @Value("${aws.s3.bucket.image_list.name}")
	    String defaultBucketName;
	    @Value("${aws.s3.bucket.image_list.original.folder}")
	    String originalImgFolder;
	    @Value("${aws.s3.bucket.image_list.edited.folder}")
	    String editedImgFolder;
	    //LOGGING
//	    @Value("${aws.s3.bucket.log.name}")
//	    String logginBucket;
//	    @Value("${ aws.s3.bucket.log.folder.name}")
//	    String logginBucketFolder;
	    String filePath="img/";
	    
    public List<String> getObjectslistFromFolder(String bucketName, String folderKey) {   
    	  ListObjectsRequest listObjectsRequest = 
    	                                new ListObjectsRequest()
    	                                      .withBucketName(bucketName)
    	                                      .withPrefix(folderKey + "/");
    	  List<String> keys = new ArrayList<>();
    	 
    	  ObjectListing objects = amazonS3Client.listObjects(listObjectsRequest);
    	  for (;;) {
    	    List<S3ObjectSummary> summaries = objects.getObjectSummaries();
    	    if (summaries.size() < 1) {
    	      break;
    	    }
    	    summaries.forEach(s -> keys.add(s.getKey()));
    	    objects = amazonS3Client.listNextBatchOfObjects(objects);
    	  }
    	 
    	  return keys;
    	}
	    
	    public byte[] getFile(String key) {
	        S3Object obj = amazonS3Client.getObject(defaultBucketName, "original/"+key);
	        S3ObjectInputStream stream = obj.getObjectContent();
	        try {
	            byte[] content = IOUtils.toByteArray(stream);
	            obj.close();
	            return content;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	    
	    public File downloadOrignalFileFromBucket_Working(String name)  {
	    	try {
	    		Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	    	boolean found=true;
	    	while(found) {
	    		List<String> objs=getObjectslistFromFolder(defaultBucketName, originalImgFolder);
		    	for	(String f:objs) {
		    		if(f.contains("original/"+name)){
		    			found =false;
		    		}
		    	}
		    	try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    	File file = new File("src/main/resources/img/original/"+name);
	        file.canWrite();
	        file.canRead();
	        byte [] originalImage=getFile(name);
	        FileOutputStream iofs = null;
	        try {
	            iofs = new FileOutputStream(file);
	            iofs.write(originalImage);
	            int result=editImage(name);
	            File editedImage=file;
	            uploadEditedImageToBucket(editedImage);
	            return editedImage;
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            return null;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	    
	    /*public File downloadOrignalFileFromBucket(String name)  {
	    	try {
	    		Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	    	boolean found=true;
	    	while(found) {
	    		List<String> objs=getObjectslistFromFolder(defaultBucketName, originalImgFolder);
		    	for	(String f:objs) {
		    		if(f.contains("original/"+name)){
		    			found =false;
		    		}
		    	}
		    	try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    	File file = new File("src/main/resources/img/original/"+name);
	        file.canWrite();
	        file.canRead();
	        byte [] originalImage=getFile(name);
	        FileOutputStream iofs = null;
	        FileOutputStream iofs2 = null;
	        try {
	            iofs = new FileOutputStream(file);
	            iofs.write(originalImage);
	            //EDIT
	            File fileEdited = new File("src/main/resources/img/edited/"+name);
	            fileEdited.canWrite();
	            fileEdited.canRead();
	            BufferedImage originBufferedImage;
				try {
					originBufferedImage = edit(file);
					byte[] editedImageInByte=convertBufferedImageToByte(originBufferedImage);
		            iofs2 = new FileOutputStream(fileEdited);
		            iofs2.write(editedImageInByte);
				} catch (Exception e) {
					e.printStackTrace();
				}
	            //END EDIT
	            uploadEditedImageToBucket(fileEdited);
	            return fileEdited;
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            return null;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	   */
	    
	    public File downloadOrignalFileFromBucket(String name) {
	    	ClassLoader classLoader = getClass().getClassLoader();
	    	try {
	    		Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	    	boolean found=true;
	    	while(found) {
	    		List<String> objs=getObjectslistFromFolder(defaultBucketName, originalImgFolder);
		    	for	(String f:objs) {
		    		if(f.contains("original/"+name)){
		    			found =false;
		    		}
		    	}
		    	try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
//	    	URL resource2 = classLoader.getResource(filePath+"original/"+name);
//            if (resource2 == null) {
//                throw new IllegalArgumentException("file not found! " + filePath);
//            }
            try {
            File file = new File("src/main/resources/img/original/"+name);
	        file.canWrite();
	        file.canRead();
	        byte [] originalImage=getFile(name);
	        FileOutputStream iofs = null;
	            iofs = new FileOutputStream(file);
	            iofs.write(originalImage);
	            //EDITING IMAGE
	            int result=editImage(name);
//	            URL resource = classLoader.getResource(filePath+"edited/"+name);
//	            if (resource == null) {
//	                throw new IllegalArgumentException("file not found! " + filePath);
//	            }
	            File editedImage=new File("src/main/resources/img/edited/"+name);
	            uploadEditedImageToBucket(editedImage);
	            return editedImage;
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            return null;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	    
	    public File dwonloadOrgFileFromBucketV2(String key) throws IOException {
	    	byte [] originalImage=getFile(key);
//	    	FileUtils.writeByteArrayToFile(new File("pathname"), originalImage);
//	    	Path path = Paths.get("src\\main\\resources\\img\\original\\"+key);
//	    	Files.write.write(path, originalImage);
	    	Path path=Files.write(new File("src\\main\\resources\\img\\test\\"+key).toPath(), originalImage);
	    	File file = path.toFile();
	    	return file;
	    }
	    
	    public int editImage(String name) throws IOException {
	    	int result=imageService.start(name);
	    	return result;
	    }
	    public byte[] convertBufferedImageToByte(BufferedImage img) {
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	try {
				ImageIO.write( img, "jpg", baos );
				baos.flush();
		    	byte[] imageInByte = baos.toByteArray();
		    	baos.close();
		    	return imageInByte;
	    	} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
	    	
	    }
	    public BufferedImage edit(File file) throws Exception {

	        BufferedImage originalImage = ImageIO.read(file);
	        BufferedImage newImage = originalImage;
	        int[] pixels = ((DataBufferInt)newImage.getRaster().getDataBuffer()).getData();

	        for(int i = 0; i < pixels.length; i++){
	            // Code for changing pixel data;
	            pixels[i] = 0xFFFFFFFF; // White
	            // Syntax for setting pixel color: 0x(HEX COLOR CODE)
	            // There is no need to set these pixels to the image; they are allerady linked
	            // For instance, if you create a Canvas object in a JFrame, 
	            // and used graphics.drawImage(newImage, 0, 0,
	            // newImage.getWidth(), newImage.getHeight(), null), it will be up to date
	            // Another example is, if you saved newImage to a file, it willallready have
	            // the white pixels drawn in.
	        }
	        return newImage;
	    }
	    
	    public void uploadEditedImageToBucket(File file){
	    	amazonS3Client.putObject(defaultBucketName, editedImgFolder+"/"+file.getName(), file);
	    }
}
