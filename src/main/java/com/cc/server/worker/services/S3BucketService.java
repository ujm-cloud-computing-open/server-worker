package com.cc.server.worker.services;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import com.amazonaws.services.s3.model.ObjectMetadata;
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
	    
	    
	    public byte[] downloadOrignalFileFromBucket(String name) {
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
		    	}}
		    	try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
            try {
	        byte [] originalImage=getFile(name);
	        InputStream is = new ByteArrayInputStream(originalImage);
	        	byte[] result=editImageV2(is);
	        	uploadEditedImageToBucketV2(result, name);
	            return result;
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
	    
	    public byte[] editImageV2(InputStream is) throws IOException {
	    	byte[] result=imageService.startV2(is);
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
	    public void uploadEditedImageToBucket(File file){
	    	amazonS3Client.putObject(defaultBucketName, editedImgFolder+"/"+file.getName(), file);
	    }
	    public void uploadEditedImageToBucketV2(byte[] content, String name){
	    	InputStream is = new ByteArrayInputStream(content);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(content.length);
            metadata.setContentType("image/jpg");
            metadata.setCacheControl("public, max-age=31536000");
	    	amazonS3Client.putObject(defaultBucketName, editedImgFolder+"/"+name, is, metadata);
	    }
}
