package kr.ac.mjc.ssacar;

import java.util.List;

public class ResponseDto {
    public List<LocationDto> getDocuments() {
        return documents;
    }

    public void setDocuments(List<LocationDto> documents) {
        this.documents = documents;
    }

    List<LocationDto> documents;

}