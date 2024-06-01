package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.comments.CreateCommentDto;
import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.CommentsRepository;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentsService {

  private final CommentsRepository commentsRepository;

  private final DinerRepository dinerRepository;

  private final MemberRepository memberRepository;

  /**
   * 식당에 코멘트 달기
   */
  public void createComment(long dinerId, CreateCommentDto dto, String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(MemberNotExistException::new);

    Diner diner = dinerRepository.findById(dinerId)
        .filter((d) -> d.getCompany().getId().equals(member.getCompany().getId()))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));

    Comments comments = Comments.builder()
        .content(dto.getContent())
        .shareStatus(dto.getShareStatus())
        .diner(diner)
        .member(member)
        .build();

    commentsRepository.save(comments);
  }
}
