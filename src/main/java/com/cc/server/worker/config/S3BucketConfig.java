
package com.cc.server.worker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

@Configuration
public class S3BucketConfig {

@Value("${cloud.aws.region.static}")
private String region;

@Value("${aws.accessKey}")
private String awsAccessKey;

@Value("${aws.secretKey}")
private String awsSecretKey;

@Value("${aws.sessionToken}")
private String sessionToken ;

@Primary
@Bean
public AmazonS3Client generateS3Client() {
	AWSSessionCredentials credentials = new BasicSessionCredentials(awsAccessKey, awsSecretKey, sessionToken);
    AmazonS3Client client = new AmazonS3Client(credentials);
	return client;
	
}
//		      @Primary
//		    @Bean
//		    public AmazonS3 generateS3() {
////		    	BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
//		    	AWSSessionCredentials credentials = new BasicSessionCredentials(awsAccessKey, awsSecretKey, sessionToken);
//		    	AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//		    			.withRegion(Regions.fromName(region))
//                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                        .build();
//		    	return s3Client;
//		    }
		    
	}


