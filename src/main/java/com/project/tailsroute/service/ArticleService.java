package com.project.tailsroute.service;

import com.project.tailsroute.repository.ArticleRepository;
import com.project.tailsroute.util.Ut;
import com.project.tailsroute.vo.Article;
import com.project.tailsroute.vo.ResultData;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Random;

@Service
public class ArticleService {

	@Autowired
	private ArticleRepository articleRepository;

	public String extractFirstImageSrc(String html) {

		Document document = Jsoup.parse(html); // HTML 파싱
		Element firstImage = document.select("img").first(); // 첫 번째 <img> 태그 선택

		if (firstImage != null) {
			return firstImage.attr("src"); // src 속성 반환
		}

		return null; // <img> 태그가 없으면 null 반환
	}

	public String removeHtmlTags(String html) {
		return html.replaceAll("<[^>]*>", "");
	}

	public ResultData writeArticle(int memberId, String title, String body, String boardId) {
		articleRepository.writeArticle(memberId, title, body, boardId);

		int id = articleRepository.getLastInsertId();

		return ResultData.from("S-1", Ut.f("%d번 글이 등록되었습니다", id), "등록 된 게시글의 id", id);
	}

	public void deleteArticle(int id) {
		articleRepository.deleteArticle(id);
	}

	public void modifyArticle(int boardId, int id, String title, String body) {
		articleRepository.modifyArticle(boardId, id, title, body);
	}

	public Article getForPrintArticle(int loginedMemberId, int id) {

		Article article = articleRepository.getForPrintArticle(id);

		controlForPrintData(loginedMemberId, article);

		return article;
	}

	public Article getArticleById(int id) {

		return articleRepository.getArticleById(id);
	}

	public List<Article> getForPrintArticles(int boardId, int itemsInAPage, int page, String searchKeywordTypeCode,
											 String searchKeyword, int memberId, String sortOrder) {

		int limitFrom = (page - 1) * itemsInAPage;
		int limitTake = itemsInAPage;

		// System.err.println("boardId : " + boardId);
		// System.err.println("limitFrom : " + limitFrom);
		// System.err.println("limitTake : " + limitTake);
		// System.err.println("searchKeywordTypeCode : " + searchKeywordTypeCode);
		// System.err.println("searchKeyword : " + searchKeyword);

		return articleRepository.getForPrintArticles(boardId, limitFrom, limitTake, searchKeywordTypeCode,
				searchKeyword, memberId, sortOrder);
	}

	public List<Article> getArticles() {
		return articleRepository.getArticles();
	}

	public Integer getCurrentArticleId() {
		return articleRepository.getCurrentArticleId();

	}

	private void controlForPrintData(int loginedMemberId, Article article) {
		if (article == null) {
			return;
		}
		ResultData userCanModifyRd = userCanModify(loginedMemberId, article);
		article.setUserCanModify(userCanModifyRd.isSuccess());

		ResultData userCanDeleteRd = userCanDelete(loginedMemberId, article);
		article.setUserCanDelete(userCanModifyRd.isSuccess());
	}

	public ResultData userCanDelete(int loginedMemberId, Article article) {
		if (article.getMemberId() != loginedMemberId) {
			return ResultData.from("F-2", Ut.f("%d번 게시글에 대한 삭제 권한이 없습니다", article.getId()));
		}
		return ResultData.from("S-1", Ut.f("%d번 게시글을 삭제했습니다", article.getId()));
	}

	public ResultData userCanModify(int loginedMemberId, Article article) {
		if (article.getMemberId() != loginedMemberId) {
			return ResultData.from("F-2", Ut.f("%d번 게시글에 대한 수정 권한이 없습니다", article.getId()));
		}
		return ResultData.from("S-1", Ut.f("%d번 게시글을 수정했습니다", article.getId()), "수정된 게시글", article);
	}

	public int getArticlesCount(int boardId, String searchKeywordTypeCode, String searchKeyword, int memberId) {
		return articleRepository.getArticleCount(boardId, searchKeywordTypeCode, searchKeyword, memberId);
	}

	public ResultData increaseHitCount(int id) {
		int affectedRow = articleRepository.increaseHitCount(id);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "해당 게시글 없음", "id", id);
		}

		return ResultData.from("S-1", "해당 게시글 조회수 증가", "id", id);

	}

	public ResultData increaseGoodReactionPoint(int relId) {
		int affectedRow = articleRepository.increaseGoodReactionPoint(relId);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "없는 게시물");
		}

		return ResultData.from("S-1", "좋아요 증가", "affectedRow", affectedRow);
	}

	public ResultData increaseBadReactionPoint(int relId) {
		int affectedRow = articleRepository.increaseBadReactionPoint(relId);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "없는 게시물");
		}

		return ResultData.from("S-1", "싫어요 증가", "affectedRow", affectedRow);
	}

	public ResultData decreaseGoodReactionPoint(int relId) {
		int affectedRow = articleRepository.decreaseGoodReactionPoint(relId);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "없는 게시물");
		}

		return ResultData.from("S-1", "좋아요 감소", "affectedRow", affectedRow);
	}

	public ResultData decreaseBadReactionPoint(int relId) {
		int affectedRow = articleRepository.decreaseBadReactionPoint(relId);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "없는 게시물");
		}

		return ResultData.from("S-1", "싫어요 감소", "affectedRow", affectedRow);
	}

	public Object getArticleHitCount(int id) {
		return articleRepository.getArticleHitCount(id);
	}

	public int getGoodRP(int relId) {
		return articleRepository.getGoodRP(relId);
	}

	public int getBadRP(int relId) {
		return articleRepository.getBadRP(relId);
	}

	public List<Article> getMainArticles(int boardId) {
		return articleRepository.getMainArticles(boardId);
	}

	public boolean isNew(String regDateStr) {
		try {
			// 날짜만 파싱하도록 수정
			String dateOnly = regDateStr.split(" ")[0]; // 시간 부분을 제거
			LocalDate regDate = LocalDate.parse(dateOnly, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			return !regDate.isBefore(LocalDate.now()); // 오늘이거나 미래 날짜일 경우 true
		} catch (DateTimeParseException e) {
			System.err.println("Invalid date format for regDate: " + regDateStr);
			return false;
		}
	}

	public String getRandomMotivation() {
		String[] motivation = {
				"행동은 모든 성공의 기초이다. - 파블로 피카소",
				"자신을 믿어라. 그러면 당신은 무엇이든 할 수 있다. - 존 우든",
				"인생은 10%가 일어나는 일이고, 90%는 그 일에 어떻게 반응하느냐이다. - 찰스 R. 스윈돌",
				"과거를 돌아보지 말고, 미래를 위해 살아라. - 에이브러햄 링컨",
				"오늘 할 수 있는 일을 내일로 미루지 마라. - 벤자민 프랭클린",
				"모든 것은 마음먹기에 달려 있다. - 세네카",
				"성공은 그저 실패한 사람들보다 더 많은 시도를 한 사람들의 결과물이다. - 토마스 에디슨",
				"진정한 용기는 두려움을 이겨내는 것이다. - 마크 트웨인",
				"우리가 할 수 있는 것은 오늘을 최선을 다하는 것이다. - 마하트마 간디",
				"매일 조금씩 성장하는 것만으로도 엄청난 변화가 온다. - 아리스토텔레스",
				"위대한 사람은 작은 일도 중요하게 생각한다. - 존 F. 케네디",
				"그대가 가는 길이 어려울지라도, 그 길을 가는 그대가 위대하다. - 헬렌 켈러",
				"기회는 준비된 사람에게 온다. - 루이스 파스퇴르",
				"결과가 중요하다면 과정도 중요하다. - 알베르트 아인슈타인",
				"인생에서 가장 중요한 것은 꾸준함이다. - 칼 루카스",
				"배움에는 끝이 없다. - 세네카",
				"지금 당신이 하는 일이 당신의 미래를 결정한다. - 제임스 알렌",
				"사람은 자신이 생각하는 대로 된다. - 나폴레옹 힐",
				"자신을 사랑하는 것이 가장 큰 성공이다. - 오프라 윈프리",
				"용기란 두려움이 없어서가 아니라 두려움을 극복하는 것이다. - 넬슨 만델라",
				"행복은 우리가 만든다. - 달라이 라마",
				"먼저 자기를 사랑하라. 그러면 세상도 당신을 사랑할 것이다. - 루이스 하우스",
				"희망은 꿈을 이루게 하는 원동력이다. - 칼 융",
				"내일을 준비하는 가장 좋은 방법은 오늘을 열심히 사는 것이다. - 헨리 포드",
				"가장 큰 영광은 결코 실패하지 않는 것이 아니라, 실패할 때마다 다시 일어나는 것이다. - 공자",
				"항상 꿈을 꾸고, 그 꿈을 위해 노력하라. - 워렌 버핏",
				"자유는 책임을 포함한다. - 조지 버나드 쇼",
				"어둠 속에서도 별은 빛난다. - 마르쿠스 아우렐리우스",
				"나쁜 경험도 결국에는 좋은 경험으로 변한다. - 프리드리히 니체",
				"세상의 모든 일은 생각에서 시작된다. - 마하트마 간디",
				"인생에서 가장 큰 기회는 어려움 속에 숨어 있다. - 앤드류 카네기",
				"단 한 번의 실패로 포기하지 말라. - 윈스턴 처칠",
				"자신감을 갖고 도전하는 것이 중요하다. - 마크 주커버그",
				"시작하는 데에 늦은 때는 없다. - 제프 베조스",
				"삶은 내가 살아가는 것이지, 다른 사람이 살아가는 것이 아니다. - 에크하르트 톨레",
				"자신을 아는 것이 가장 큰 지혜이다. - 소크라테스",
				"성공은 끊임없는 노력이다. - 콜린 파월",
				"진정한 변화는 내면에서 시작된다. - 스티브 잡스",
				"끝없는 노력만이 진정한 성공을 이끈다. - 앨버트 아인슈타인",
				"지혜는 경험에서 온다. - 헨리 포드",
				"항상 자신의 길을 가라. - 바바라 포스터",
				"행복은 자신이 원하는 것을 찾는 것이 아니라, 자신이 가진 것에 감사하는 것이다. - 조지 샤우어",
				"포기하지 않으면 결국 해낼 수 있다. - 찰리 채플린",
				"당신이 오늘 한 작은 일이 내일의 큰 변화를 만든다. - 파블로 피카소",
				"불가능을 믿지 말라. - 윌리엄 셰익스피어",
				"자신의 꿈을 이루려면 노력해야 한다. - 마크 트웨인",
				"인생에서 가장 중요한 것은 자신을 믿는 것이다. - 헤르만 헤세",
				"행복은 선택이다. - 라오쯔",
				"꿈은 큰 일을 이끌어낸다. - 조지 버나드 쇼",
				"과거는 이미 지나갔고, 미래는 아직 오지 않았다. 그러므로 현재를 소중히 여겨라. - 불교 명언",
				"자신이 무엇을 원하고 있는지 아는 것이 중요하다. - 프리드리히 니체",
				"성공은 준비된 자에게 온다. - 칼 융",
				"끝까지 포기하지 말고, 스스로를 믿어라. - 마이클 조던",
				"자신을 사랑하는 것이 가장 중요한 일이다. - 루이스 하우스",
				"행복은 마음가짐에 달려 있다. - 불교 명언",
				"희망을 잃지 않으면 결국 목표를 달성할 수 있다. - 에이브러햄 링컨",
				"자신을 믿는 순간, 세상이 달라 보인다. - 시드니 시먼스",
				"성공은 많은 실패와 그 실패를 극복하려는 의지에서 나온다. - 윈스턴 처칠",
				"가장 중요한 것은 진실되게 살아가는 것이다. - 장 자크 루소",
				"꿈을 쫓아가는 사람만이 인생의 진정한 의미를 찾을 수 있다. - 윌리엄 제임스",
				"자기 자신을 믿고 나아가는 길이 진정한 길이다. - 버나드 쇼",
				"하루에 하나씩 배우는 것이 가장 큰 진보다. - 루이스 카롤",
				"포기하지 않는 한, 실패는 없다. - 앤디 워홀",
				"희망이 없으면 삶은 끝난 것이다. - 프랭클린 D. 루스벨트",
				"우리는 늘 원하는 것을 얻기 위해 노력한다. - 마하트마 간디",
				"어려움 속에서도 희망을 잃지 마라. - 링컨",
				"성공은 고통 속에서 나온다. - 안나 아퀴넴",
				"자신의 길을 믿고 걸어라. - 제프리 드요르",
				"인생은 결국 선택이다. - 에리히 프롬",
				"기회를 만드는 것은 끊임없는 노력이다. - 오프라 윈프리",
				"하루라도 빨리 행복을 찾으려면 오늘을 살아라. - 오프라 윈프리",
				"세상을 바꾸려면 자신을 바꿔라. - 마하트마 간디",
				"꿈을 추구하는 사람은 아무리 어려운 일이 있어도 계속 나아갈 수 있다. - 빌 게이츠",
				"자신을 잃지 않는 것이 가장 중요한 일이다. - 헨리 포드",
				"최고의 자신을 보여라. - 조앤 롤링",
				"오늘을 최선을 다해 살아라. - 헨리 데이비드 소로",
				"용기는 두려움을 극복하는 것이다. - 마하트마 간디",
				"포기하지 말고 계속 도전하라. - 존 F. 케네디",
				"삶은 늘 선택의 연속이다. - 유키카즈",
				"성공은 결국 끈기와 노력이 만든다. - 벤저민 프랭클린",
				"꿈을 이루는 것은 누구에게나 가능하다. - 오프라 윈프리",
				"행복은 자기가 선택한 삶을 사는 것이다. - 마하트마 간디",
				"내일의 나를 위해 오늘을 열심히 살아라. - 톰 피터슨",
				"미래는 항상 당신의 손에 달려 있다. - 마하트마 간디",
				"내일을 위해 오늘을 사라. - 마르크스 아우렐리우스",
				"진실된 열정은 사람들을 이끈다. - 마르쿠스 아우렐리우스",
				"모든 것은 내 마음에서 시작된다. - 시도",
				"인생은 그 자체로 아름다워야 한다. - 샤르로트 브론테",
				"어떤 일이든 도전하고 나서 이루면 그것이 진정한 성취이다. - 에리카 종",
				"누군가를 도와줄 때 가장 큰 기쁨을 느낄 수 있다. - 마하트마 간디",
				"불가능을 가능으로 바꾸는 것은 오직 당신의 의지와 능력이다. - 오프라 윈프리",
				"위험을 감수하는 것이 인생을 더욱 풍요롭게 만든다. - 앨버트 아인슈타인",
				"가장 중요한 것은 도전이다. - 윈스턴 처칠",
				"자신의 꿈을 믿고 이루어 가라. - 토마스 에디슨",
				"삶의 중요한 것은 도전하는 것이다. - 프랭클린 D. 루스벨트",
				"자신을 믿으면 반드시 목표를 이룰 수 있다. - 조지 워싱턴",
				"자신의 길을 믿고 가라. - 헨리 데이비드 소로",
				"최고의 자신을 끊임없이 추구하라. - 빌 게이츠",
				"인생은 주어진 시간 안에서 최선을 다하는 것이다. - 장 파울",
				"항상 최선을 다하는 것이 중요하다. - 마이클 조던"
		};

		Random random = new Random();
		int index = random.nextInt(motivation.length);
		return motivation[index];
	}
}