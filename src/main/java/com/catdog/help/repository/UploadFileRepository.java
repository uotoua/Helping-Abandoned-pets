package com.catdog.help.repository;

import com.catdog.help.domain.Board.UploadFile;

import java.util.List;

public interface UploadFileRepository {

    public Long save(UploadFile uploadFile);

    public List<UploadFile> findUploadFiles(Long boardId);
}
