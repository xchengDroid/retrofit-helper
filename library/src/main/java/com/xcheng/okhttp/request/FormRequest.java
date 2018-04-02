package com.xcheng.okhttp.request;

import android.support.annotation.Nullable;

import com.xcheng.okhttp.util.ParamUtil;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 提交Form表单，默认为POST请求。如果有文件则MediaType 为 multipart/form-data,否则为application/x-www-form-urlencoded
 * Created by cx on 17/6/22.
 */
public class FormRequest extends OkRequest {

    private final List<FileInput> fileInputs;
    private final boolean multipart;

    private FormRequest(Builder builder) {
        super(builder);
        this.fileInputs = ParamUtil.immutableList(builder.fileInputs);
        this.multipart = builder.multipart;
    }

    @Override
    public Request createRequest() {
        RequestBody requestBody;
        if (ParamUtil.isEmpty(fileInputs) && !multipart) {
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
                builder.addFormDataPart(fileInput.name, fileInput.file.getName(), fileBody);
            }
            requestBody = builder.build();
        }
        return new Request.Builder().url(url()).tag(tag()).headers(headers()).method(method(), requestBody).build();
    }

    private static String guessMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(fileName);
        return ParamUtil.defValueIfNull(contentTypeFor, MEDIA_TYPE_STREAM.toString());
    }

    private void addParams(MultipartBody.Builder builder) {
        Map<String, String> params = params();
        if (!ParamUtil.isEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addParams(FormBody.Builder builder) {
        Map<String, String> params = params();
        if (!ParamUtil.isEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
    }

    public static class Builder extends OkRequest.Builder<Builder> {
        private final List<FileInput> fileInputs = new ArrayList<>();
        private boolean multipart = false;

        public Builder addFileInput(FileInput fileInput) {
            fileInputs.add(fileInput);
            return this;
        }

        /**
         * @param multipart true 代表没有文件的时候依然使用multipart/form-data
         */
        public Builder multipart(boolean multipart) {
            this.multipart = multipart;
            return this;
        }

        public Builder addFileInput(String name, @Nullable MediaType mediaType, File file) {
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
        public FormRequest build() {
            super.build();
            return new FormRequest(this);
        }
    }

    public static class FileInput {
        private String name;
        private MediaType mediaType;
        private File file;

        public FileInput(String name, File file) {
            this(name, MediaType.parse(guessMimeType(file.getName())), file);
        }

        public FileInput(String name, @Nullable MediaType mediaType, File file) {
            this.name = name;
            this.mediaType = mediaType;
            this.file = file;
        }

        @Override
        public String toString() {
            return "FileInput{" +
                    "name='" + name + '\'' +
                    ", mediaType='" + mediaType + '\'' +
                    ", file=" + file +
                    '}';
        }
    }
}
