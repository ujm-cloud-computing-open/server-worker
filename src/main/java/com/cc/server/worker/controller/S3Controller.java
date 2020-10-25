package com.cc.server.worker.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.QueueMessageVisibility;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.util.json.Jackson;
import com.cc.server.worker.model.LogginModel;
import com.cc.server.worker.model.NumberListRequest;
import com.cc.server.worker.services.LogginService;
import com.cc.server.worker.services.S3BucketService;
import com.cc.server.worker.services.SqsService;
import com.cc.server.worker.websocket.config.Message_Handler_Singleton;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
@RestController
@RequestMapping(value = "/api/s3")

@CrossOrigin
public class S3Controller {
//	@Autowired
//	AmazonS3Client amazonS3Client;
	@Autowired
	S3BucketService s3bucketService;
	//IMAGE BUCKET
    @Value("${aws.s3.bucket.image_list.name}")
    String defaultBucketName;
    @Value("${aws.s3.bucket.image_list.original.folder}")
    String originalImgFolder;
    @Value("${aws.s3.bucket.image_list.edited.folder}")
    String editedImgFolder;
    //LOGGING
//    @Value("${aws.s3.bucket.log.name}")
//    String logginBucket;
//    
//    @Value("${ aws.s3.bucket.log.folder.name}")
//    String logginBucketFolder;
   
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private SqsService sqsService;
    private Message_Handler_Singleton messagePip;
    @Autowired
    private LogginService logginService;
    
    private static final String reciever_queue_image="sqs_image_reciever_poll";
    private static final String sender_queue_image="sqs_image_sender_poll";	
    public static final Logger LOGGER = LoggerFactory.getLogger(SqsController.class);
    
//    PERFECTLY WORKING
//    @CrossOrigin
//    @SqsListener(value = sender_queue_image, deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
//	public void getMessageFromSqs( String message, 
//			  @Header("MessageId") String messageId,
//			  @Header("LogicalResourceId") String logicalResourceId,
//			  @Header("ApproximateReceiveCount") String approximateReceiveCount,
//			  @Header("ApproximateFirstReceiveTimestamp") String approximateFirstReceiveTimestamp,
//			  @Header("SentTimestamp") String sentTimestamp,
//			  @Header("ReceiptHandle") String receiptHandle,
//			  @Header("Visibility") QueueMessageVisibility visibility,
//			  @Header("SenderId") String senderId,
//			  @Header("contentType") String contentType,
//			  @Header("lookupDestination") String lookupDestination
//	  ) {
//    	System.out.println("SREVER WORKER RECIEVED THE MESSAGE>>"+message);
//			LOGGER.info("Received image queue message= {}", message);
//			ObjectMapper mapper = new ObjectMapper();
//			String key="";
//			try {
//				 key = mapper.readValue(message, String.class);
//			} catch (JsonMappingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JsonProcessingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			File editedImage=s3bucketService.downloadOrignalFileFromBucket(key);
//			String messageBody=Jackson.toJsonString(editedImage.getName());
//			sqsService.sendMessages(reciever_queue_image, messageBody);
//			LOGGER.info("Successfully Dispatched to queue");
//    	}
    
    @CrossOrigin
    @SqsListener(value = sender_queue_image, deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void getMessageFromSqs( String message, 
			  @Header("MessageId") String messageId,
			  @Header("LogicalResourceId") String logicalResourceId,
			  @Header("ApproximateReceiveCount") String approximateReceiveCount,
			  @Header("ApproximateFirstReceiveTimestamp") String approximateFirstReceiveTimestamp,
			  @Header("SentTimestamp") String sentTimestamp,
			  @Header("ReceiptHandle") String receiptHandle,
			  @Header("Visibility") QueueMessageVisibility visibility,
			  @Header("SenderId") String senderId,
			  @Header("contentType") String contentType,
			  @Header("lookupDestination") String lookupDestination
	  ) {
    	System.out.println("SREVER WORKER RECIEVED THE MESSAGE>>"+message);
			LOGGER.info("Received image queue message= {}", message);
			LogginModel logModel=new LogginModel();
   			logModel.messageId=messageId;
   			logModel.message=message;
   			logModel.logicalResourceId=logicalResourceId;
   			logModel.approximateReceiveCount=approximateReceiveCount;
   			logModel.approximateFirstReceiveTimestamp=approximateFirstReceiveTimestamp;
   			logModel.sentTimestamp=sentTimestamp;
   			logModel.receiptHandle=receiptHandle;
   			logModel.senderId=senderId;
   			logModel.contentType=contentType;
   			logModel.lookupDestination=lookupDestination;
   			try {
				logginService.addLoggin(logModel);
				String logText=messageId+"\n"+message+"\n"+logicalResourceId+
						"\n"+approximateReceiveCount+"\n"+approximateFirstReceiveTimestamp
						+"\n"+sentTimestamp+"\n"+receiptHandle
						+"\n"+senderId+"\n"+contentType
						+"\n"+lookupDestination;
				logginService.addLogginString(logText);
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			ObjectMapper mapper = new ObjectMapper();
			String key="";
			try {
				 key = mapper.readValue(message, String.class);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			s3bucketService.downloadOrignalFileFromBucket(key);
			String messageBody=Jackson.toJsonString(key);
			sqsService.sendMessages(reciever_queue_image, messageBody);
			LOGGER.info("Successfully Dispatched to queue");
    	}

    @GetMapping(path = "/download")
    public ResponseEntity<ByteArrayResource> uploadFile(@RequestParam(value = "file") String file) throws IOException {
        byte[] data = s3bucketService.getFile(file);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + file + "\"")
                .body(resource);
    }
    
    @GetMapping(path = "/dt")
    public String testImageDownloadFromS3() {
    		String key="xx.jpg";
			byte[] editedImage=s3bucketService.getFile("xx.jpg");
			LOGGER.info("Successfully Dispatched to queue");
			return editedImage.toString();
  	}
    
//    @GetMapping(path = "/dt2")
//    public String uploadFile() throws IOException {
//        File data = s3bucketService.downloadOrignalFileFromBucket("f.jpg");
//        return "ok";
//    }
    
}
