package com.marceldev.companylunchcomment.repository.company;

import com.marceldev.companylunchcomment.entity.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, Long> {

  boolean existsByDomainAndName(String domain, String name);

  @Query("select c "
      + "from Company c "
      + "join Diner d on c.id = d.company.id "
      + "join Comment cm on d.id = cm.diner.id "
      + "join Reply r on cm.id = r.comment.id "
      + "where r.id = :replyId")
  Optional<Company> findCompanyByReplyId(@Param("replyId") long id);

  @Query("select c "
      + "from Company c "
      + "join Diner d on c.id = d.company.id "
      + "join Comment cm on d.id = cm.diner.id "
      + "where cm.id = :commentId")
  Optional<Company> findCompanyByCommentId(@Param("commentId") long id);
}
