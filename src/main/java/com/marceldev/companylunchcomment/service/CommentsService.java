package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.comments.CommentsOutputDto;
import com.marceldev.companylunchcomment.dto.comments.CreateCommentDto;
import com.marceldev.companylunchcomment.dto.comments.GetCommentsListDto;
import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.CommentsNotFoundException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.mapper.CommentsMapper;
import com.marceldev.companylunchcomment.repository.CommentsRepository;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentsService {

  private final CommentsRepository commentsRepository;

  private final DinerRepository dinerRepository;

  private final MemberRepository memberRepository;

  private final CommentsMapper commentsMapper;

  /**
   * 식당에 코멘트 작성
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

  /**
   * 식당의 코멘트 조회
   */
  @Transactional
  public Page<CommentsOutputDto> getCommentsList(long dinerId, Authentication auth,
      GetCommentsListDto dto) {
    String myName = memberRepository.findByEmail(auth.getName())
        .map(Member::getName)
        .orElseThrow(MemberNotExistException::new);
    PageRequest pageable = PageRequest.of(dto.getPage(), dto.getPageSize());

    long total = commentsMapper.selectListCount(dinerId, myName, dto);
    List<CommentsOutputDto> commentsList = commentsMapper.selectList(
        dinerId,
        myName, // 자신이 남긴 코멘트는 shareStatus가 ME라도 볼러와야 하기 때문에 필요
        dto,
        pageable,
        dto.getCommentsSort().toString() // mybatis의 경우 pageable 속 sort를 사용하지 않고, 별도로 넣어줌
    );

    return new PageImpl<>(commentsList, pageable, total);
  }

  /**
   * 코멘트 삭제. 자신의 email로 되어 있는 코멘트만 삭제 가능함
   */
  public void deleteComments(long commentsId, String email) {
    Comments comments = commentsRepository.findByIdAndMember_Email(commentsId, email)
        .orElseThrow(CommentsNotFoundException::new);
    commentsRepository.delete(comments);
  }
}
