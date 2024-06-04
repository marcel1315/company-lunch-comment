package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.comments.CreateCommentDto;
import com.marceldev.companylunchcomment.dto.reply.CreateReplyDto;
import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.Reply;
import com.marceldev.companylunchcomment.exception.CommentsNotFoundException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.CommentsRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyService {

  private final ReplyRepository replyRepository;

  private final CommentsRepository commentsRepository;

  private final MemberRepository memberRepository;

  /**
   * 댓글 작성
   */
  public void createReply(long commentId, CreateReplyDto dto, String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(MemberNotExistException::new);

    Comments comments = commentsRepository.findById(commentId)
        .orElseThrow(CommentsNotFoundException::new);

    Reply reply = Reply.builder()
        .content(dto.getContent())
        .comments(comments)
        .member(member)
        .build();

    replyRepository.save(reply);
  }
}
