package com.github.rholder.esthree.cli;

import com.amazonaws.services.s3.AmazonS3Client;
import com.github.rholder.esthree.AmazonS3ClientMockUtils;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class GetCommandTest {

    public static final int CHUNK_SIZE = 1000;
    public static int EXPECTED_CHUNKS = 1;

    @Test
    public void happyPath() throws IOException {
        AmazonS3Client client = AmazonS3ClientMockUtils.createMockedClient(CHUNK_SIZE, EXPECTED_CHUNKS);

        GetCommand getCommand = new GetCommand();
        getCommand.progress = false;
        getCommand.parameters = Lists.newArrayList("s3://foo/bar.txt");
        getCommand.amazonS3Client = client;

        getCommand.parse();
    }

    @Test
    public void happyPathWithTargetFile() throws IOException {
        AmazonS3Client client = AmazonS3ClientMockUtils.createMockedClient(CHUNK_SIZE, EXPECTED_CHUNKS);

        GetCommand getCommand = new GetCommand();
        getCommand.progress = false;
        getCommand.parameters = Lists.newArrayList("s3://foo/bar.txt", "baz.txt");
        getCommand.amazonS3Client = client;

        getCommand.parse();
    }

    @Test
    public void garbagePath() throws IOException {
        GetCommand getCommand = new GetCommand();
        getCommand.progress = false;
        getCommand.parameters = Lists.newArrayList("potato");

        try {
            getCommand.parse();
            Assert.fail("Expected an IllegalArgumentException for garbage path");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("target filename"));
        }
    }

    @Test
    public void bucketWithNoFilename() throws IOException {
        GetCommand getCommand = new GetCommand();
        getCommand.progress = false;
        getCommand.parameters = Lists.newArrayList("s3://foo");

        try {
            getCommand.parse();
            Assert.fail("Expected an IllegalArgumentException for garbage path");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("target filename"));
        }
    }

    @Test
    public void bucketWithBlankFilename() throws IOException {
        GetCommand getCommand = new GetCommand();
        getCommand.progress = false;
        getCommand.parameters = Lists.newArrayList("s3://foo/");

        try {
            getCommand.parse();
            Assert.fail("Expected an IllegalArgumentException for garbage path");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("target filename"));
        }
    }
}