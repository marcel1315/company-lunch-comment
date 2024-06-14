package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.reply.CreateReplyDto;
import com.marceldev.companylunchcomment.dto.reply.ReplyOutputDto;
import com.marceldev.companylunchcomment.dto.reply.UpdateReplyDto;
import com.marceldev.companylunchcomment.entity.Comment;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.Reply;
import com.marceldev.companylunchcomment.exception.CommentNotFoundException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.exception.MemberUnauthorizedException;
import com.marceldev.companylunchcomment.exception.ReplyNotFoundException;
import com.marceldev.companylunchcomment.repository.comment.CommentRepository;
import com.marceldev.companylunchcomment.repository.diner.DinerRepository;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import com.marceldev.companylunchcomment.repository.reply.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

  private final ReplyRepository replyRepository;

  private final CommentRepository commentRepository;

  private final MemberRepository memberRepository;

  private final DinerRepository dinerRepository;

  /**
   * 댓글 작성
   */
  @Transactional
  public void createReply(long dinerId, long commentId, CreateReplyDto dto) {
    checkDiner(dinerId);

    Member member = getMember();

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(CommentNotFoundException::new);

    Reply reply = Reply.builder()
        .content(dto.getContent())
        .comment(comment)
        .member(member)
        .build();

    replyRepository.save(reply);
  }

  /**
   * 댓글 조회
   */
  public Page<ReplyOutputDto> getReplyList(long dinerId, long commentId, Pageable pageable) {
    checkDiner(dinerId);

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(CommentNotFoundException::new);

    switch (comment.getShareStatus()) {
      case COMPANY:
        break;
      case ME:
        if (!comment.getMember().equals(getMember())) {
          throw new MemberUnauthorizedException();
        }
        break;
    }

    return replyRepository.findByCommentIdOrderByCreatedAtDesc(commentId, pageable)
        .map(ReplyOutputDto::of);
  }

  /**
   * 댓글 수정
   */
  @Transactional
  public void updateReply(long dinerId, long replyId, UpdateReplyDto dto) {
    checkDiner(dinerId);

    Member member = getMember();

    Reply reply = replyRepository.findById(replyId)
        .filter((r) -> r.getMember().getId().equals(member.getId()))
        .orElseThrow(ReplyNotFoundException::new);

    reply.setContent(dto.getContent());
  }

  /**
   * 댓글 삭제
   */
  @Transactional
  public void deleteReply(long dinerId, long replyId) {
    checkDiner(dinerId);

    Member member = getMember();

    Reply reply = replyRepository.findById(replyId)
        .filter((r) -> r.getMember().getId().equals(member.getId()))
        .orElseThrow(ReplyNotFoundException::new);

    replyRepository.delete(reply);
  }

  /**
   * member를 찾아 반환함
   */
  private Member getMember() {
    UserDetails user = (UserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    String email = user.getUsername();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotExistException::new);
  }

  /**
   * diner에 접근할 수 있는지 검사
   */
  private void checkDiner(long dinerId) {
    // member의 company를 체크하고, diner의 company를 체크해서 둘이 같은지 비교
    Member member = getMember();
    Diner diner = dinerRepository.findById(dinerId)
        .orElseThrow(() -> new DinerNotFoundException(dinerId));
    if (!member.getCompany().getId().equals(diner.getCompany().getId())) {
      throw new MemberUnauthorizedException();
    }
  }
}
