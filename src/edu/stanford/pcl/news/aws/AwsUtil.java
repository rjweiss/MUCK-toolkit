package edu.stanford.pcl.news.aws;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import edu.stanford.pcl.news.NewsProperties;
import org.glassfish.jersey.internal.util.Base64;

public class AwsUtil {

    private static void wait(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static String getEc2InstanceMetadata(String name) {
        try {
            URL url = new URL(String.format("http://169.254.169.254/latest/meta-data/%s", name));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(500);
            connection.setUseCaches(false);
            if (connection.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // XXX  Only handles single-line metadata values.
                String response = in.readLine();
                return response;
            }
        }
        catch (SocketException e) {
            // This is expected when not running on EC2.  XXX  Not so sure what to do about this if we are running in EC2.
        }
        catch (Exception e) {
//            log.severe(String.format("%s in AwsUtil.getEc2InstanceMetadata(): %s", e.getClass().getSimpleName(), e.getMessage()));
        }
        return null;
    }

    public static String getPrivateIpv4Address() {
        return getEc2InstanceMetadata("local-ipv4");
    }

    public static String getPublicIpv4Address() {
        return getEc2InstanceMetadata("public-ipv4");
    }

    public static String getInstancePublicIpv4Address(String instanceId) {
        AWSCredentials credentials = new BasicAWSCredentials(NewsProperties.getProperty("aws.access.key"), NewsProperties.getProperty("aws.secret.key"));
        AmazonEC2 ec2 = new AmazonEC2Client(credentials);
//        List<String> instanceAddresses = new ArrayList<String>();
        DescribeInstancesResult describeInstancesResult = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));
        List<Reservation> reservations = describeInstancesResult.getReservations();
        for (Reservation reservation : reservations) {
            List<com.amazonaws.services.ec2.model.Instance> ec2Instances = reservation.getInstances();
            for (com.amazonaws.services.ec2.model.Instance instance : ec2Instances) {
//                instanceAddresses.add(instance.getPublicIpAddress());
                return instance.getPublicIpAddress();
            }
        }
        return null;
    }

    public static String createUserDataScript(String... commands) {
        StringBuilder sb = new StringBuilder("#!/bin/bash\n");
        for (String command : commands) {
            sb.append(command).append("\n");
        }
        return Base64.encodeAsString(sb.toString());
    }

    public static String startInstance(String name, String ami, String instanceType, String keyPairName, String securityGroupName, String userData) {
        AWSCredentials credentials = new BasicAWSCredentials(NewsProperties.getProperty("aws.access.key"), NewsProperties.getProperty("aws.secret.key"));
        AmazonEC2 ec2 = new AmazonEC2Client(credentials);

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(ami)
                .withInstanceType(instanceType)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyPairName)
                .withSecurityGroups(securityGroupName);

        if (userData != null) {
            runInstancesRequest.withUserData(userData);
        }

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);

        System.out.println("reservation id: " + runInstancesResult.getReservation().getReservationId());

        Reservation reservation = runInstancesResult.getReservation();
        List<Instance> instances = reservation.getInstances();

        List<String> instanceIds = new ArrayList<String>();
        for (Instance instance : instances) {
            instanceIds.add(instance.getInstanceId());
        }

        CreateTagsRequest createTagsRequest = new CreateTagsRequest();
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag("Name", name));
        createTagsRequest.setTags(tags);
        createTagsRequest.withResources(instanceIds);
        ec2.createTags(createTagsRequest);

        return instanceIds.get(0);
    }

    public static ArrayList<String> startSpotInstances(String namePrefix, int number, String price, String ami, String instanceType, String keyPairName, String securityGroupName, String userData) {
        AWSCredentials credentials = new BasicAWSCredentials(NewsProperties.getProperty("aws.access.key"), NewsProperties.getProperty("aws.secret.key"));
        AmazonEC2 ec2 = new AmazonEC2Client(credentials);

        // Set up and send the spot request.
        RequestSpotInstancesRequest spotRequest = new RequestSpotInstancesRequest();
        spotRequest.setSpotPrice(price);
        spotRequest.setInstanceCount(number);
        spotRequest.setType(SpotInstanceType.OneTime);

        LaunchSpecification launchSpecification = new LaunchSpecification();
        launchSpecification.setImageId(ami);
        launchSpecification.setInstanceType(instanceType);
        launchSpecification.withKeyName(keyPairName);

        if (userData != null) {
            launchSpecification.withUserData(userData);
        }

        ArrayList<String> securityGroups = new ArrayList<String>();
        securityGroups.add(securityGroupName);
        launchSpecification.setSecurityGroups(securityGroups);

        spotRequest.setLaunchSpecification(launchSpecification);

        RequestSpotInstancesResult spotResult = ec2.requestSpotInstances(spotRequest);


        // Check on the spot requests.
        List<SpotInstanceRequest> spotResponses = spotResult.getSpotInstanceRequests();

        ArrayList<String> spotInstanceRequestIds = new ArrayList<String>();
        for (SpotInstanceRequest requestResponse : spotResponses) {
            System.out.print("\nspot request id: " + requestResponse.getSpotInstanceRequestId());
            spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
        }

        ArrayList<String> instanceIds = new ArrayList<String>();
        boolean anyOpen;
        Map<String, Integer> previousStatus = new HashMap<String, Integer>();

        do {
            wait(10);

            DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
            describeRequest.setSpotInstanceRequestIds(spotInstanceRequestIds);

            anyOpen = false;

            try {
                DescribeSpotInstanceRequestsResult describeResult = ec2.describeSpotInstanceRequests(describeRequest);
                List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();

                Map<String, Integer> status = new HashMap<String, Integer>();
                for (SpotInstanceRequest describeResponse : describeResponses) {
                    Integer count = status.get(describeResponse.getStatus().getCode());
                    if (count == null) count = 0;
                    status.put(describeResponse.getStatus().getCode(), count + 1);
                    if (describeResponse.getState().equals("open")) {
                        anyOpen = true;
                        break;
                    }

                    instanceIds.add(describeResponse.getInstanceId());
                }

                if (!status.equals(previousStatus)) {
                    for (String code : status.keySet()) {
                        System.out.print(String.format("\n%s: %d", code, status.get(code)));
                        System.out.flush();
                    }
                }
                else {
                    System.out.print(".");
                    System.out.flush();
                }
                previousStatus = status;

            } catch (AmazonServiceException e) {
                anyOpen = true;
            }

        } while (anyOpen);

        System.out.println();

        int i=1;
        for (String instanceId : instanceIds) {
            CreateTagsRequest createTagsRequest = new CreateTagsRequest();
            List<Tag> tags = new ArrayList<Tag>();
            tags.add(new Tag("Name", String.format("%s %d", namePrefix, i++)));
            createTagsRequest.setTags(tags);
            createTagsRequest.withResources(instanceId);
            ec2.createTags(createTagsRequest);
        }

        return instanceIds;
    }

    public static void waitForInstances(List<String> instanceIds) {
        AWSCredentials credentials = new BasicAWSCredentials(NewsProperties.getProperty("aws.access.key"), NewsProperties.getProperty("aws.secret.key"));
        AmazonEC2 ec2 = new AmazonEC2Client(credentials);

        boolean allRunning = false;
        DescribeInstancesRequest instancesRequest = new DescribeInstancesRequest();
        instancesRequest.withInstanceIds(instanceIds);

        while (!allRunning) {
            wait(10);

            allRunning = true;
            DescribeInstancesResult result = ec2.describeInstances(instancesRequest);

            reservationLoop:
            for (Reservation reservation : result.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    if (!instance.getState().getName().equals("running")) {
                        allRunning = false;
                        break reservationLoop;
                    }
                }
            }
        }

        boolean allOk = false;
        DescribeInstanceStatusRequest statusRequest = new DescribeInstanceStatusRequest();
        statusRequest.withInstanceIds(instanceIds);

        while (!allOk) {
            wait(10);

            allOk = true;
            DescribeInstanceStatusResult result = ec2.describeInstanceStatus(statusRequest);
            for (InstanceStatus status : result.getInstanceStatuses()) {
                if (!status.getInstanceStatus().getStatus().equals("ok")) {
                    allOk = false;
                    break;
                }
            }
        }
    }

}
