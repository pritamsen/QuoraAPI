package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthenticationEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

@RestController
@RequestMapping(path = "/question")
public class QuestionController {

    @Autowired
    QuestionBusinessService questionBusinessService;

    @Autowired
    UserBusinessService userBusinessService;

    @RequestMapping(method = RequestMethod.POST, path = "/create"
            , consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
            , produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") String authorization
            , QuestionRequest questionRequest) throws AuthorizationFailedException {
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setUuid(UUID.randomUUID().toString());
        ZonedDateTime now = ZonedDateTime.now();
        questionEntity.setDateTime(now);
        QuestionEntity question = questionBusinessService.createQuestion(authorization, questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(question.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.GET, path = "/all"
            , produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(
            @RequestHeader("authorization") String authorization)
            throws AuthorizationFailedException {

        userBusinessService.authorizeUser(authorization,"GetAllQuestions");
        List<QuestionEntity> questionEntities = questionBusinessService.getAllQuestion();
        return getListResponseEntity(questionEntities);

    }

    @RequestMapping(method = RequestMethod.PUT, path = "/edit/{questionId}"
            , produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestion(
            @RequestHeader("authorization") String authorization,
            @PathVariable(value = "questionId") String questionId
            , QuestionEditRequest questionEditRequest) throws AuthorizationFailedException, InvalidQuestionException {

        UserAuthenticationEntity userAuthenticationEntity = userBusinessService.authorizeUser(authorization,"EditQuestion");
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(questionId);
        questionEntity.setContent(questionEditRequest.getContent());
        QuestionEntity updatedQuestionEntity = questionBusinessService.editQuestion(questionEntity, userAuthenticationEntity.getUserEntity());
        QuestionEditResponse editedQuestion = new QuestionEditResponse().id(updatedQuestionEntity.getUuid())
                .status("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(editedQuestion, HttpStatus.OK);


    }


    @RequestMapping(method = RequestMethod.DELETE, path = "/delete/{questionId}"
            , produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(
            @RequestHeader("authorization") String authorization,
            @PathVariable(value = "questionId") String questionId)
            throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthenticationEntity userAuthenticationEntity = userBusinessService.authorizeUser(authorization,"DeleteQuestion");
        QuestionEntity deteledQuestionEntity = questionBusinessService.deleteQuestion(questionId, userAuthenticationEntity.getUserEntity());
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse()
                .status("QUESTION DELETED")
                .id(deteledQuestionEntity.getUuid());
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);

    }


    @RequestMapping(method = RequestMethod.GET, path = "/all/{userId}"
            , produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(
            @RequestHeader("authorization") String authorization
            , @PathVariable(name = "userId") String userId)
            throws AuthorizationFailedException, UserNotFoundException {

        UserAuthenticationEntity userAuthenticationEntity =  userBusinessService.authorizeUser(authorization, "GetAllQuestionsByUser");
        UserEntity user = userBusinessService.userProfile(userId,authorization);

        List<QuestionEntity> questionsByUser = questionBusinessService.getAllQuestionbyUser(user);

        return getListResponseEntity(questionsByUser);

    }

    private ResponseEntity<List<QuestionDetailsResponse>> getListResponseEntity(List<QuestionEntity> questionsByUser) {
        List<QuestionDetailsResponse> questionDetailsResponses = new ArrayList<>();
        ListIterator<QuestionEntity> questionEntityListIterator = questionsByUser.listIterator();
        while (questionEntityListIterator.hasNext()) {
            QuestionEntity questionEntity = questionEntityListIterator. next();
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse()
                    .content(questionEntity.getContent())
                    .id(questionEntity.getUuid());
            questionDetailsResponses.add(questionDetailsResponse);
        }

        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponses, HttpStatus.OK);
    }


}
