package com.evilbeast.meizi.utils;

import android.util.Log;

import com.evilbeast.meizi.entity.fuli.FuliItemObject;
import com.evilbeast.meizi.entity.meizi.MeiZi;
import com.evilbeast.meizi.entity.meizi.MeiZiGroup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Author: sumary
 */
public class MeiZiUtil {
    private static volatile MeiZiUtil mInstance;
    private MeiZiUtil() {}

    // 单例模式
    public static MeiZiUtil getInstance() {
        if (mInstance == null) {
            synchronized (MeiZiUtil.class) {
                if (mInstance == null) {
                    mInstance = new MeiZiUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 解析妹子图html
     *
     * @param html
     * @param type
     * @return
     */
    public List<MeiZi> parserMeiziTuHtml(String html, String type)
    {

        List<MeiZi> list = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("li");

        Element aelement;
        Element imgelement;
        for (int i = 7; i < links.size(); i++)
        {
            imgelement = links.get(i).select("img").first();
            aelement = links.get(i).select("a").first();
            MeiZi bean = new MeiZi();
            bean.setOrder(i);

            bean.setTitle(imgelement.attr("alt").toString());
            bean.setType(type);
            bean.setHeight(354);
            bean.setWidth(236);
            bean.setImageurl(imgelement.attr("data-original"));
            bean.setUrl(aelement.attr("href"));
            bean.setGroupid(url2groupid(bean.getUrl()));
            list.add(bean);
        }
        return list;
    }

    /**
     * 获取妹子图的GroupId
     *
     * @param url
     * @return
     */
    private int url2groupid(String url)
    {

        return Integer.parseInt(url.split("/")[3]);
    }

    public void putMeiZiCache(List<MeiZi> list) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(list);
        realm.commitTransaction();
        realm.close();
    }

    public void putGroupCache(List<MeiZiGroup> list) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
        realm.close();
    }

    public List<MeiZiGroup> parseMeiZiGroupSave(String html, int groupId) {
        List<MeiZiGroup> results = new ArrayList<>();
        String keyNum;
        String imageUrl;
        Document doc = Jsoup.parse(html);
        String url = doc.select("div.main-image img").first().attr("src");
        int total_page = Integer.parseInt(doc.select("div.pagenavi span.dots ~ a span").first().text());
        String urlPrefix = url.substring(0, url.lastIndexOf("/"));
        String pathName = url.substring(url.lastIndexOf("/")+1);
        String keyPrefix = pathName.split("01")[0];
        String keyExt = pathName.split("01")[1];

        for (int i = 1; i <= total_page; i++) {
            keyNum = String.format("%2d", i).replace(' ', '0');
            imageUrl = String.format("%s/%s%s%s", urlPrefix, keyPrefix, keyNum, keyExt);

            MeiZiGroup data = new MeiZiGroup();
            data.setGroupId(groupId);
            data.setImageUrl(imageUrl);
            results.add(data);
        }
        return results;
    }

    public List<FuliItemObject> parseFuliItems(String html) {
        List<FuliItemObject> results = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements aTags = doc.select("div.movie-item > a");
        LogUtil.all(aTags.size()+"#");
        for (int i = 0; i < aTags.size(); i++) {
            Element aTag = aTags.get(i);
            String title = aTag.attr("title");

            String href = aTag.attr("href");
            int groupId = Integer.parseInt(href.substring(href.lastIndexOf("/")+1, href.lastIndexOf(".")));
            String imageUrl = aTag.select("img").first().attr("src");

            FuliItemObject item = new FuliItemObject();
            item.setImageUrl(imageUrl);
            item.setGroupId(groupId);
            item.setTitle(title);

            results.add(item);
        }


        LogUtil.all(results.toString());
        return results;
    }

    public void putFuliItemCache(final List<FuliItemObject> list) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(list);
            }
        });
    }
}
