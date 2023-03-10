package com.catdog.help.web.controller;

import com.catdog.help.FileStore;
import com.catdog.help.service.BulletinBoardService;
import com.catdog.help.web.SessionConst;
import com.catdog.help.web.dto.BulletinBoardDto;
import com.catdog.help.web.form.bulletinboard.PageBulletinBoardForm;
import com.catdog.help.web.form.bulletinboard.UpdateBulletinBoardForm;
import com.catdog.help.web.form.bulletinboard.SaveBulletinBoardForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BulletinBoardController {

    private final BulletinBoardService bulletinBoardService;
    private final FileStore fileStore;


    @GetMapping("/boards/new")
    public String createBulletinBoardForm(@SessionAttribute(name = SessionConst.LOGIN_USER) String nickName, Model model) {
        SaveBulletinBoardForm saveBoardForm = new SaveBulletinBoardForm();
        model.addAttribute("nickName", nickName);
        model.addAttribute("saveBoardForm", saveBoardForm);
        return "bulletinBoard/create";
    }

    @PostMapping("/boards/new")
    public String createBulletinBoard(@SessionAttribute(name = SessionConst.LOGIN_USER) String nickName,
                                      @Validated @ModelAttribute("saveBoardForm") SaveBulletinBoardForm saveBoardForm,
                                      BindingResult bindingResult, RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            return "bulletinBoard/create";
        }
        Long boardId = bulletinBoardService.createBoard(saveBoardForm, nickName);
        redirectAttributes.addAttribute("id", boardId);
        return "redirect:/boards/{id}";
    }

    @GetMapping("/boards")
    public String BulletinBoardList(Model model) {
        List<PageBulletinBoardForm> pageBoardForms = bulletinBoardService.readAll();
        model.addAttribute("pageBoardForms", pageBoardForms);
        return "bulletinBoard/list";
    }

    @GetMapping("/boards/{id}")
    public String readBulletinBoard(@PathVariable("id") Long id, Model model,
                                    @SessionAttribute(name = SessionConst.LOGIN_USER) String nickName) {
        BulletinBoardDto bulletinBoardDto = bulletinBoardService.readBoard(id);
        model.addAttribute("bulletinBoardDto", bulletinBoardDto);
        model.addAttribute("nickName", nickName); // detail ????????????
        return "bulletinBoard/detail";
    }

    @ResponseBody
    @GetMapping("/images/{fileName}")
    public Resource downloadImage(@PathVariable String fileName) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(fileName));
    }

    @GetMapping("/boards/{id}/edit")
    public String updateBulletinBoardForm(@PathVariable("id") Long id, Model model,
                                          @SessionAttribute(name = SessionConst.LOGIN_USER) String nickName) {
        //????????? ????????? ?????? ??????
        BulletinBoardDto findBoardDto = bulletinBoardService.readBoard(id);
        if (!findBoardDto.getUser().getNickName().equals(nickName)) {
            return "redirect:/boards/{id}";
        }
        UpdateBulletinBoardForm updateBulletinBoardForm = bulletinBoardService.getUpdateForm(id);
        model.addAttribute("updateBoardForm", updateBulletinBoardForm);
        model.addAttribute("nickName", nickName);
        return "bulletinBoard/update";
    }

    @PostMapping("/boards/{id}/edit")
    public String updateBulletinBoard(@Validated @ModelAttribute("updateBoardForm") UpdateBulletinBoardForm updateBulletinBoardForm,
                                      BindingResult bindingResult) throws IOException {
        // TODO: 2023-03-12 ????????? ????????? ??? ?????? ???????????? ????????? ??? ???

        if (bindingResult.hasErrors()) {
            return "bulletinBoard/update";
        }
        bulletinBoardService.updateBoard(updateBulletinBoardForm);
        return "redirect:/boards/{id}";
    }

    @GetMapping("/boards/{id}/delete")
    public String deleteBulletinBoardForm(@PathVariable("id") Long id, Model model,
                                          @SessionAttribute(name = SessionConst.LOGIN_USER) String nickName) {
        //????????? ????????? ?????? ??????
        BulletinBoardDto findBoardDto = bulletinBoardService.readBoard(id);
        if (!findBoardDto.getUser().getNickName().equals(nickName)) {
            return "redirect:/boards/{id}";
        }
        String boardTitle = findBoardDto.getTitle();
        model.addAttribute("boardId", id);
        model.addAttribute("boardTitle", boardTitle);
        model.addAttribute("nickName", nickName);
        return "bulletinBoard/delete";
    }

    @PostMapping("/boards/{id}/delete")
    public String deleteBulletinBoard(@PathVariable("id") Long id, Model model,
                                      @SessionAttribute(name = SessionConst.LOGIN_USER) String nickName) {
        //????????? ????????? ?????? ??????
        BulletinBoardDto findBoardDto = bulletinBoardService.readBoard(id);
        if (!findBoardDto.getUser().getNickName().equals(nickName)) {
            return "redirect:/boards/{id}";
        }
        bulletinBoardService.deleteBoard(id);
        return "redirect:/boards";
    }
}
