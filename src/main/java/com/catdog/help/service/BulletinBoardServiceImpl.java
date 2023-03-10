package com.catdog.help.service;

import com.catdog.help.FileStore;
import com.catdog.help.domain.Board.UploadFile;
import com.catdog.help.domain.User;
import com.catdog.help.repository.UploadFileRepository;
import com.catdog.help.repository.UserRepository;
import com.catdog.help.web.dto.BulletinBoardDto;
import com.catdog.help.web.form.bulletinboard.PageBulletinBoardForm;
import com.catdog.help.web.form.bulletinboard.UpdateBulletinBoardForm;
import com.catdog.help.domain.Board.BulletinBoard;
import com.catdog.help.repository.BulletinBoardRepository;
import com.catdog.help.web.form.bulletinboard.SaveBulletinBoardForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BulletinBoardServiceImpl implements BulletinBoardService {

    private final BulletinBoardRepository bulletinBoardRepository;
    private final UploadFileRepository uploadFileRepository;
    private final UserRepository userRepository;
    private final FileStore fileStore;

    @Transactional
    public Long createBoard(SaveBulletinBoardForm boardForm, String nickName) throws IOException {
        User findUser = userRepository.findByNickName(nickName);
        BulletinBoard board = createBulletinBoard(boardForm, findUser);
        Long boardId = bulletinBoardRepository.save(board); //cascade All 설정 후 리스트에 추가해서 보드만 저장해도 될듯!? 고민 필요
        for (UploadFile image : board.getImages()) {
            uploadFileRepository.save(image);
        }
        return boardId;
    }

    public BulletinBoardDto readBoard(Long id) {
        BulletinBoard findBoard = bulletinBoardRepository.findOne(id);
        User user = findBoard.getUser();
        log.info("User={}", user); // TODO: 2023-03-06 지연로딩 이라 일단 로그로 호출
        List<UploadFile> uploadFiles = uploadFileRepository.findUploadFiles(id);
        BulletinBoardDto bulletinBoardDto = getBulletinBoardDto(findBoard, user, uploadFiles);
        return bulletinBoardDto;
    }

    public List<PageBulletinBoardForm> readAll() {
        List<PageBulletinBoardForm> pageBoardForms = new ArrayList<>();
        List<BulletinBoard> boards = bulletinBoardRepository.findAll();
        for (BulletinBoard board : boards) {
            User user = board.getUser();
            log.info("User={}", user); // TODO: 2023-03-06 지연로딩 이라 일단 로그로 호출
            pageBoardForms.add(getPageBulletinBoardForm(board, user.getNickName())); // TODO: 2023-03-12 작동 잘되면 User 대신 nickName으로 시도
        }
        return pageBoardForms;
    }

    public UpdateBulletinBoardForm getUpdateForm(Long id) {
        BulletinBoard findBoard = bulletinBoardRepository.findOne(id);
        UpdateBulletinBoardForm updateForm = new UpdateBulletinBoardForm();
        updateForm.setId(findBoard.getId());
        updateForm.setRegion(findBoard.getRegion());
        updateForm.setTitle(findBoard.getTitle());
        updateForm.setContent(findBoard.getContent());
        //images 는 생성과 동시에 초기화
        updateForm.setWriteDate(findBoard.getWriteDate()); //수정된 날짜로 변경
        return updateForm;
    }

    @Transactional
    public Long updateBoard(UpdateBulletinBoardForm updateForm) throws IOException {
        BulletinBoard findBoard = bulletinBoardRepository.findOne(updateForm.getId());
        List<UploadFile> uploadFiles = uploadFileRepository.findUploadFiles(findBoard.getId());
        updateBulletinBoard(findBoard, updateForm);  //변경감지 이용한 덕분에 user 값 변경없이 수정이 된다!
        return findBoard.getId();
    }

    @Transactional
    public void deleteBoard(Long boardId) {
        BulletinBoard findBoard = bulletinBoardRepository.findOne(boardId);
        bulletinBoardRepository.delete(findBoard);
    }


    /**============================= private method ==============================*/

    private BulletinBoard createBulletinBoard(SaveBulletinBoardForm boardForm, User findUser) throws IOException {
        BulletinBoard board = new BulletinBoard();
        board.setUser(findUser);
        board.setRegion(boardForm.getRegion());
        board.setTitle(boardForm.getTitle());
        board.setContent(boardForm.getContent());
        if (!boardForm.getImages().isEmpty()) {
            List<UploadFile> uploadFiles = fileStore.storeFiles(boardForm.getImages());
            for (UploadFile uploadFile : uploadFiles) {
                board.addImage(uploadFile); //uploadFile 에 board 주입
            }
        }
        board.setScore(0);
        board.setWriteDate(LocalDateTime.now());
        return board;
    }

    private BulletinBoardDto getBulletinBoardDto(BulletinBoard findBoard, User user, List<UploadFile> uploadFiles) {
        BulletinBoardDto bulletinBoardDto = new BulletinBoardDto();
        bulletinBoardDto.setId(findBoard.getId());
        bulletinBoardDto.setUser(user);
        bulletinBoardDto.setRegion(findBoard.getRegion());
        bulletinBoardDto.setTitle(findBoard.getTitle());
        bulletinBoardDto.setContent(findBoard.getContent());
        bulletinBoardDto.setImages(uploadFiles);
        bulletinBoardDto.setWriteDate(findBoard.getWriteDate());
        bulletinBoardDto.setScore(findBoard.getScore());
        return bulletinBoardDto;
    }

    private PageBulletinBoardForm getPageBulletinBoardForm(BulletinBoard board, String nickName) {
        PageBulletinBoardForm boardForm = new PageBulletinBoardForm();
        boardForm.setId(board.getId());
        boardForm.setRegion(board.getRegion());
        boardForm.setTitle(board.getTitle());
        boardForm.setUserNickName(nickName);
        boardForm.setWriteDate(board.getWriteDate());
        return boardForm;
    }

    private void updateBulletinBoard(BulletinBoard findBoard, UpdateBulletinBoardForm updateForm) throws IOException {
        findBoard.setRegion(updateForm.getRegion());
        findBoard.setTitle(updateForm.getTitle());
        findBoard.setContent(updateForm.getContent());
        List<UploadFile> uploadFiles = fileStore.storeFiles(updateForm.getImages());
        for (UploadFile uploadFile : uploadFiles) {
            findBoard.addImage(uploadFile);
            uploadFileRepository.save(uploadFile);
        }
    }

}
