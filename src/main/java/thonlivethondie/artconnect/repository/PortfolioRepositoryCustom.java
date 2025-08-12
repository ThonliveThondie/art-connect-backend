package thonlivethondie.artconnect.repository;

import thonlivethondie.artconnect.entity.User;

import java.util.List;

public interface PortfolioRepositoryCustom {

    /**
     * 주어진 키워드 목록을 사용하여 디자이너를 검색합니다.
     *
     * @param keywords 검색할 키워드 목록
     * @return 키워드와 매칭되는 디자이너 목록 (중복 제거됨)
     */
    List<User> findDesignersByKeywords(List<String> keywords);
}
