package thonlivethondie.artconnect.util;

import java.util.Random;

/**
 * 닉네임을 자동으로 생성하는 유틸리티 클래스
 * 형용사 + 명사 + 숫자 조합으로 의미있는 닉네임을 생성합니다.
 */
public class NicknameGenerator {

  private static final Random random = new Random();
  
  private static final String[] ADJECTIVES = {
    "행복한", "멋진", "창의적인", "열정적인", "친근한", "활발한", "유쾌한", "신비한"
  };
  
  private static final String[] NOUNS = {
    "아티스트", "디자이너", "크리에이터", "예술가", "작가", "화가", "조각가", "사진가"
  };

  private NicknameGenerator() {
    // 유틸리티 클래스이므로 인스턴스 생성 방지
  }

  /**
   * 랜덤한 닉네임을 생성합니다.
   * 
   * @return 형용사 + 명사 + 숫자(1-9999) 조합의 닉네임
   */
  public static String generateRandomNickname() {
    String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
    String noun = NOUNS[random.nextInt(NOUNS.length)];
    int randomNumber = random.nextInt(9999) + 1;
    
    return adjective + noun + randomNumber;
  }
}
