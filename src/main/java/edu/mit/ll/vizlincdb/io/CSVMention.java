package edu.mit.ll.vizlincdb.io;

import java.util.Objects;

/**
 *  CSVMention follows Bean protocol, and is used by CSVMentionFileReader.
 */
    /**
     *
     */
    public class CSVMention {
        private String entityType;
        private int startPosition;
        private int stopPosition;
        private int indexInDocument;
        private Integer globalId;   // may be null
        private String mentionText;

        public CSVMention() {
        }

    public CSVMention(String entityType, int startPosition, int stopPosition, int indexInDocument, Integer globalId, String mentionText) {
        this.entityType = entityType;
        this.startPosition = startPosition;
        this.stopPosition = stopPosition;
        this.indexInDocument = indexInDocument;
        this.globalId = globalId;
        this.mentionText = mentionText;
    }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public void setStartPosition(int startPosition) {
            this.startPosition = startPosition;
        }

        public int getStopPosition() {
            return stopPosition;
        }

        public void setStopPosition(int stopPosition) {
            this.stopPosition = stopPosition;
        }

        public int getIndexInDocument() {
            return indexInDocument;
        }

        public void setIndexInDocument(int indexInDocument) {
            this.indexInDocument = indexInDocument;
        }

        public Integer getGlobalId() {
            return globalId;
        }

        public void setGlobalId(Integer globalId) {
            this.globalId = globalId;
        }

        public String getMentionText() {
            return mentionText;
        }

        public void setMentionText(String mentionText) {
            this.mentionText = mentionText;
        }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.entityType);
        hash = 97 * hash + this.startPosition;
        hash = 97 * hash + this.stopPosition;
        hash = 97 * hash + this.indexInDocument;
        hash = 97 * hash + Objects.hashCode(this.globalId);
        hash = 97 * hash + Objects.hashCode(this.mentionText);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CSVMention other = (CSVMention) obj;
        if (!Objects.equals(this.entityType, other.entityType)) {
            return false;
        }
        if (this.startPosition != other.startPosition) {
            return false;
        }
        if (this.stopPosition != other.stopPosition) {
            return false;
        }
        if (this.indexInDocument != other.indexInDocument) {
            return false;
        }
        if (!Objects.equals(this.globalId, other.globalId)) {
            return false;
        }
        if (!Objects.equals(this.mentionText, other.mentionText)) {
            return false;
        }
        return true;
    }



    }
