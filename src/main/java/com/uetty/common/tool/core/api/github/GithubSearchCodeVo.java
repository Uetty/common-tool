package com.uetty.common.tool.core.api.github;

import java.util.List;

/**
 * github search code 接口的返回值封装
 */
@SuppressWarnings("unused")
public class GithubSearchCodeVo extends GithubSearchBaseVo {

    private Long totalCount;
    private Integer currentPageNo;
    private Integer currentPageSize;
    private String searchKey;
    private String scope;
    private Boolean incompleteResults;

    private List<ResultItem> items;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCurrentPageNo() {
        return currentPageNo;
    }

    public void setCurrentPageNo(Integer currentPageNo) {
        this.currentPageNo = currentPageNo;
    }

    public Integer getCurrentPageSize() {
        return currentPageSize;
    }

    public void setCurrentPageSize(Integer currentPageSize) {
        this.currentPageSize = currentPageSize;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getIncompleteResults() {
        return incompleteResults;
    }

    public void setIncompleteResults(Boolean incompleteResults) {
        this.incompleteResults = incompleteResults;
    }

    public List<ResultItem> getItems() {
        return items;
    }

    public void setItems(List<ResultItem> items) {
        this.items = items;
    }

    public static class ResultItem {
        private String fileName;
        private String fileSha;
        private String filePath;
        private String contentUrl;
        private Long repoId;
        private String repoFullName;
        private Long owerId;
        private String ownerName;
        private String ownerHtmlUrl;
        private String fileHtmlUrl;
        private String repoHtmlUrl;
        private String matchMetadata; // 匹配的元数据
        private Boolean trueMatch = true; // 是否真的匹配，针对github存在对字符串拆分匹配的情况
        private Long index; // 在总记录中是第几条

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileSha() {
            return fileSha;
        }

        public void setFileSha(String fileSha) {
            this.fileSha = fileSha;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getContentUrl() {
            return contentUrl;
        }

        public void setContentUrl(String contentUrl) {
            this.contentUrl = contentUrl;
        }

        public Long getRepoId() {
            return repoId;
        }

        public void setRepoId(Long repoId) {
            this.repoId = repoId;
        }

        public String getRepoFullName() {
            return repoFullName;
        }

        public void setRepoFullName(String repoFullName) {
            this.repoFullName = repoFullName;
        }

        public Long getOwerId() {
            return owerId;
        }

        public void setOwerId(Long owerId) {
            this.owerId = owerId;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public String getOwnerHtmlUrl() {
            return ownerHtmlUrl;
        }

        public void setOwnerHtmlUrl(String ownerHtmlUrl) {
            this.ownerHtmlUrl = ownerHtmlUrl;
        }

        public String getFileHtmlUrl() {
            return fileHtmlUrl;
        }

        public void setFileHtmlUrl(String fileHtmlUrl) {
            this.fileHtmlUrl = fileHtmlUrl;
        }

        public String getRepoHtmlUrl() {
            return repoHtmlUrl;
        }

        public void setRepoHtmlUrl(String repoHtmlUrl) {
            this.repoHtmlUrl = repoHtmlUrl;
        }

        public String getMatchMetadata() {
            return matchMetadata;
        }

        public void setMatchMetadata(String matchMetadata) {
            this.matchMetadata = matchMetadata;
        }

        public Long getIndex() {
            return index;
        }

        public void setIndex(Long index) {
            this.index = index;
        }

        /**
         * 是否真的匹配，针对github存在对字符串拆分匹配的情况
         */
        public Boolean getTrueMatch() {
            return trueMatch == null ? true : trueMatch;
        }

        public void setTrueMatch(Boolean trueMatch) {
            this.trueMatch = trueMatch;
        }
    }
}
