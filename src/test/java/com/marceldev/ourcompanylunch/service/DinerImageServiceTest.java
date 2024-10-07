package com.marceldev.ourcompanylunch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.marceldev.ourcompanylunch.basic.IntegrationTest;
import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyRequest;
import com.marceldev.ourcompanylunch.dto.diner.AddDinerImageResponse;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerResponse;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerImage;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.diner.DinerImageNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerMaxImageCountExceedException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.ImageDeleteFailException;
import com.marceldev.ourcompanylunch.exception.diner.ImageReadFailException;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class DinerImageServiceTest extends IntegrationTest {

  @Test
  @DisplayName("Add diner image - Success")
  void test_update_diner_add_image() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    MultipartFile mockImageFile = createMockImageFile();

    // when
    AddDinerImageResponse response = dinerImageService.addDinerImage(diner.getId(), mockImageFile);

    // then
    entityManager.clear(); // To get updated entity from DB(Not persistent context)
    Diner savedDiner = dinerRepository.findById(diner.getId()).orElseThrow();
    assertThat(savedDiner.getDinerImages()).hasSize(2)
        .extracting("orders")
        .containsExactly(100, 200); // TODO: Fix that thumbnail is included in order.
  }

  @Test
  @DisplayName("Add diner image - Fail(Diner not found)")
  void test_update_diner_add_image_fail_no_diner() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    // Not saving diner

    MultipartFile mockImageFile = createMockImageFile();

    // when // then
    assertThatThrownBy(() -> dinerImageService.addDinerImage(1L, mockImageFile))
        .isInstanceOf(DinerNotFoundException.class);
  }

  @Test
  @DisplayName("Add diner image - Fail(More than 10 images exist)")
  void test_update_diner_add_image_fail_max_count() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    for (int i = 1; i <= 10; i++) {
      MultipartFile mockImageFile = createMockImageFile();
      dinerImageService.addDinerImage(diner.getId(), mockImageFile);
    }

    MultipartFile mockImageFile = createMockImageFile();

    // when // then
    assertThrows(DinerMaxImageCountExceedException.class,
        () -> dinerImageService.addDinerImage(diner.getId(), mockImageFile));
  }

  @Test
  @DisplayName("Add diner image - Fail(Image file can't be read)")
  void test_update_diner_add_image_fail_cant_read_image() throws Exception {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    MultipartFile wrongMockImageFile = new MockMultipartFile(
        "food.jpg",
        "food.jpg",
        "image/jpeg",
        "not_image".getBytes()
    );

    // when // then
    assertThatThrownBy(() -> dinerImageService.addDinerImage(diner.getId(), wrongMockImageFile))
        .isInstanceOf(ImageReadFailException.class);
  }

  @Test
  @DisplayName("Remove diner image - Success")
  void test_update_diner_remove_image() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    MultipartFile mockImageFile = createMockImageFile();
    AddDinerImageResponse response = dinerImageService.addDinerImage(diner.getId(), mockImageFile);

    // when
    dinerImageService.removeDinerImage(response.getId());

    //then
    Optional<DinerImage> dinerImage = dinerImageRepository.findById(response.getId());
    assertThat(dinerImage).isEmpty();
  }

  @Test
  @DisplayName("Remove diner image - Fail(Image not found)")
  void test_update_diner_remove_image_fail_image_not_found() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");
    // Not saving image

    // when // then
    assertThatThrownBy(() -> dinerImageService.removeDinerImage(1L))
        .isInstanceOf(DinerImageNotFoundException.class);
  }

  @Test
  @DisplayName("Remove diner image - Fail(Fail to remove in S3)")
  void test_update_diner_remove_image_fail_s3_delete_fail() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    MultipartFile mockImageFile = createMockImageFile();
    AddDinerImageResponse response = dinerImageService.addDinerImage(diner.getId(), mockImageFile);

    // when
    doThrow(new RuntimeException())
        .when(s3Manager).removeFile(any());

    // then
    assertThatThrownBy(() -> dinerImageService.removeDinerImage(1L))
        .isInstanceOf(ImageDeleteFailException.class);
  }

  // --- Create fixture ---

  private MultipartFile createMockImageFile() {
    ClassPathResource imageFile = new ClassPathResource("food.jpg");

    try {
      return new MockMultipartFile(
          "food.jpg",
          imageFile.getFilename(),
          "image/jpeg",
          imageFile.getInputStream()
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // --- Save some entity ---

  private Company saveCompany() {
    Company company = Company.builder()
        .name("HelloCompany")
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .enterKey("company123")
        .enterKeyEnabled(false)
        .location(LocationUtil.createPoint(37.123456, 127.123456))
        .build();
    return companyRepository.save(company);
  }

  private Member saveMember() {
    Member member = Member.builder()
        .name("Jack")
        .email("jack@example.com")
        .company(null)
        .role(Role.VIEWER)
        .build();
    return memberRepository.save(member);
  }

  private void chooseCompany(Company company) {
    ChooseCompanyRequest chooseCompanyRequest = new ChooseCompanyRequest("company123");
    companyService.chooseCompany(company.getId(), chooseCompanyRequest);
  }

  private Diner saveDiner(String name) {
    CreateDinerRequest request = CreateDinerRequest.builder()
        .name(name)
        .link("diner.com")
        .latitude(37.29283882)
        .longitude(127.39232323)
        .tags(new LinkedHashSet<>())
        .build();
    CreateDinerResponse response = dinerService.createDiner(request);
    return dinerRepository.findById(response.getId()).orElseThrow();
  }
}