package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.dto.reply.CreateReplyDto;
import com.marceldev.ourcompanylunch.dto.reply.GetReplyListDto;
import com.marceldev.ourcompanylunch.dto.reply.ReplyOutputDto;
import com.marceldev.ourcompanylunch.dto.reply.UpdateReplyDto;
import com.marceldev.ourcompanylunch.entity.Comment;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.Reply;
import com.marceldev.ourcompanylunch.exception.comment.CommentNotFoundException;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.exception.reply.ReplyNotFoundException;
import com.marceldev.ourcompanylunch.repository.comment.CommentRepository;
import com.marceldev.ourcompanylunch.repository.company.CompanyRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.reply.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

  private final ReplyRepository replyRepository;

  private final CommentRepository commentRepository;

  private final MemberRepository memberRepository;

  private final CompanyRepository companyRepository;

  @Transactional
  public void createReply(long commentId, CreateReplyDto dto) {
    checkDinerByCommentId(commentId);

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

  public Page<ReplyOutputDto> getReplyList(long commentId, GetReplyListDto dto) {
    checkDinerByCommentId(commentId);

    Pageable pageable = PageRequest.of(
        dto.getPage(),
        dto.getSize()
    );

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

  @Transactional
  public void updateReply(long replyId, UpdateReplyDto dto) {
    checkDinerByReplyId(replyId);

    Member member = getMember();

    Reply reply = replyRepository.findById(replyId)
        .filter((r) -> r.getMember().getId().equals(member.getId()))
        .orElseThrow(ReplyNotFoundException::new);

    reply.setContent(dto.getContent());
  }

  @Transactional
  public void deleteReply(long replyId) {
    checkDinerByReplyId(replyId);

    Member member = getMember();

    Reply reply = replyRepository.findById(replyId)
        .filter((r) -> r.getMember().getId().equals(member.getId()))
        .orElseThrow(ReplyNotFoundException::new);

    replyRepository.delete(reply);
  }

  private Member getMember() {
    String email = (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotFoundException::new);
  }

  /**
   * Check if the member can access the diner.
   */
  private void checkDinerByCommentId(long id) {
    Member member = getMember();

    Company company = companyRepository.findCompanyByCommentId(id)
        .orElseThrow(CompanyNotFoundException::new);

    if (!member.getCompany().getId().equals(company.getId())) {
      throw new MemberUnauthorizedException();
    }
  }

  /**
   * Check if the member can access the diner.
   */
  private void checkDinerByReplyId(long id) {
    Member member = getMember();

    Company company = companyRepository.findCompanyByReplyId(id)
        .orElseThrow(CompanyNotFoundException::new);

    if (!member.getCompany().getId().equals(company.getId())) {
      throw new MemberUnauthorizedException();
    }
  }
}
