/*
 * Copyright 2014 Ray Holder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rholder.esthree.cli;

import com.github.rholder.esthree.command.GetMultipart;
import com.github.rholder.esthree.progress.MutableProgressListener;
import com.github.rholder.esthree.progress.PrintingProgressListener;
import com.github.rholder.esthree.util.S3PathUtils;
import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;

import java.io.File;
import java.util.List;

import static com.google.common.base.Objects.firstNonNull;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FilenameUtils.getPrefixLength;

@Command(name = "get-multi", description = "Download a file from S3, but download in multiple parts")
public class GetMultipartCommand extends EsthreeCommand {

    @Option(name = {"-c", "--chunk-size"}, arity = 1, description = "The request chunk size in bytes (e.g. 10485760 for 10MB chunks), defaults to 5MB")
    public Integer chunkSize;

    @Option(name = {"-np", "--no-progress"}, description = "Don't print a progress bar")
    public Boolean progress;

    @Arguments(usage = "<target bucket and key> [optional target file]", description = "The target bucket and key, as in \"s3://bucket/foo.html\"")
    public List<String> parameters;

    @Override
    public void run() {
        if(help) {
            showUsage(commandMetadata);
            return;
        }

        if(firstNonNull(parameters, emptyList()).size() == 0) {
            showUsage(commandMetadata);
            throw new IllegalArgumentException("No arguments specified");
        }

        String target = parameters.get(0);
        String bucket = S3PathUtils.getBucket(target);
        String key = S3PathUtils.getPrefix(target);
        progress = progress == null;

        // TODO validate get-multi params here
        File outputFile;
        if(parameters.size() > 1) {
            outputFile = new File(parameters.get(1));
        } else {
            // infer filename from file being fetched if unspecified
            String path = S3PathUtils.getPrefix(target);
            if(path == null) {
                throw new IllegalArgumentException("Could not determine target filename from " + target);
            }

            int index = path.lastIndexOf(File.separatorChar);
            int prefixLength = getPrefixLength(path);

            String fileName;
            if (index < prefixLength) {
                fileName =  path.substring(prefixLength);
            } else {
                fileName = path.substring(index + 1);
            }
            outputFile = new File(fileName);
        }

        try {
            MutableProgressListener progressListener = null;
            if(progress) {
                progressListener = new PrintingProgressListener(output);
            }

            new GetMultipart(amazonS3Client, bucket, key, outputFile)
                    .withChunkSize(chunkSize)
                    .withProgressListener(progressListener)
                    .call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
