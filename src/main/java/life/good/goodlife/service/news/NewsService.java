package life.good.goodlife.service.news;

import life.good.goodlife.model.news.Article;

public interface NewsService {
    Article[] getNews(int count, int offset, String category);
}
