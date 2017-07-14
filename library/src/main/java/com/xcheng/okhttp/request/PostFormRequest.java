package com.xcheng.okhttp.request;

import com.xcheng.okhttp.utils.ParamHelper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by cx on 17/6/22.
 */
public class PostFormRequest extends OkRequest {
    private static MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");

    private List<FileInput> fileInputs;

    protected PostFormRequest(Builder builder) {
        super(builder);
        this.fileInputs = builder.fileInputs;
    }

    @Override
    protected Request createRequest() {
        RequestBody requestBody;
        if (fileInputs == null || fileInputs.isEmpty()) {
            FormBody.Builder builder = new FormBody.Builder();
            addParams(builder);
            requestBody = builder.build();
        } else {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            addParams(builder);
            for (int i = 0; i < fileInputs.size(); i++) {
                FileInput fileInput = fileInputs.get(i);
                RequestBody fileBody = RequestBody.create(fileInput.mediaType, fileInput.file);
                builder.addFormDataPart(fileInput.key, fileInput.file.getName(), fileBody);
            }
            requestBody = builder.build();
        }
        return new Request.Builder().url(url()).tag(tag()).headers(headers().build()).post(requestBody).build();
    }

    private static String guessMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (contentTypeFor == null) {
            contentTypeFor = MEDIA_TYPE_STREAM.toString();
        }
        return contentTypeFor;
    }

    private void addParams(MultipartBody.Builder builder) {
        Map<String, String> params = params();
        if (!ParamHelper.checkMapEmpty(params)) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key, params.get(key));
            }
        }
    }

    private void addParams(FormBody.Builder builder) {
        Map<String, String> params = params();
        if (!ParamHelper.checkMapEmpty(params)) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
    }

    public static class Builder extends OkRequestBuilder<Builder> {
        private final List<FileInput> fileInputs = new ArrayList<>();

        public Builder addFileInput(FileInput fileInput) {
            fileInputs.add(fileInput);
            return this;
        }

        public Builder addFileInput(String name, MediaType mediaType, File file) {
            fileInputs.add(new FileInput(name, mediaType, file));
            return this;
        }

        /**
         * guess mediaType
         */
        public Builder addFileInput(String name, File file) {
            fileInputs.add(new FileInput(name, file));
            return this;
        }

        @Override
        public PostFormRequest build() {
            return new PostFormRequest(this);
        }
    }

    public static class FileInput {
        private String key;
        private MediaType mediaType;
        private File file;

        public FileInput(String name, File file) {
            this.key = name;
            this.mediaType = MediaType.parse(guessMimeType(file.getName()));
            this.file = file;
        }

        public FileInput(String name, MediaType mediaType, File file) {
            this.key = name;
            this.mediaType = mediaType;
            this.file = file;
        }

        @Override
        public String toString() {
            return "FileInput{" +
                    "key='" + key + '\'' +
                    ", mediaType='" + mediaType + '\'' +
                    ", file=" + file +
                    '}';
        }
    }
}
