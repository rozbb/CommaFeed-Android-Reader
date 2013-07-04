package com.commafeed.commafeedreader;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.googlecode.androidannotations.annotations.rest.Get;
import com.googlecode.androidannotations.annotations.rest.Post;
import com.googlecode.androidannotations.annotations.rest.Rest;

// Ensure that my ignorance of API changes doesn't break the client
@JsonIgnoreProperties(ignoreUnknown = true)

// DO NOT INSTANTIATE CLIENTS FROM HERE
// Use RestProxy.getInstance() to get the single client object which
// handles errors gracefully
@Rest(converters = { MappingJacksonHttpMessageConverter.class })
public interface CommaFeedClient {
	
	public void setRootUrl(String rootUrl);
	
	/*
	 * Category API
	 * ############
	 */
	
	@Post("/category/collapse")
	void categoryCollapse(CollapseRequest collapseRequest);
	
	@Get("/category/unreadCount")
	UnreadCount categoryUnreadCount();
	
	@Post("/category/mark")
	void categoryMark(MarkRequest markRequest);
	
	/*@Get("/category/entriesAsFeed?id={id}")
	??? categoryEntriesAsFeed(int id);*/
	
	@Post("/category/add")
	void categoryAdd(AddCategoryRequest addCategoryRequest);
	
	// See feedEntries
	@Get("/category/entries?id={id}&readType={readType}&newerThan={newerThan}&offset={offset}&limit={limit}&order={order}")
	Entries categoryEntries(String id, String readType, long newerThan, int offset, int limit, String order);
	
	@Post("/category/delete")
	void categoryDelete(IDRequest idRequest);
	
	@Post("/category/modify")
	void categoryModify(CategoryModificationRequest categoryModificationRequest);
	
	@Get("/category/get")
	Category categoryGet();
	
	
	/*
	 * Server API
	 * ##########
	 */
	
	@Get("/server/get")
	ServerInfo serverGet();
	
	RestTemplate getRestTemplate();
	void setRestTemplate(RestTemplate restTemplate);


	/*
	 * User API
	 * ########
	 */

	@Get("/user/settings")
	Settings userSettings();
	
	@Post("/user/settings")
	void userSettings(Settings settings);
	
	@Get("/user/profile")
	UserModel userProfile();
	
	@Post("/user/profile")
	void userProfile(UserModel userModel);
	
	@Post("/user/profile/deleteAccount")
	void userProfileDeleteAccount();
	
	
	/*
	 * Entry API
	 * #########
	 */
	
	@Post("/entry/mark")
	void entryMark(MarkRequest markRequest);
	
	@Post("/entry/star")
	void entryStar(StarRequest starRequest);
	
	@Get("/entry/search")
	Entries entrySearch();
	

	/*
	 * Feed API
	 * ########
	 */

	@Get("/feed/subscribe?url={url}")
	void feedSubscribe(String url);
	
	@Post("/feed/subscribe")
	void feedSubscribe(SubscribeRequest subscribeRequest);
	
	/*@Post("/feed/import")			// Save for distant future
	void feedImport(String string);*/
	
	@Post("/feed/favicon/{id}")
	void feedFaviconId(Long id);
	
	@Post("/feed/modify")
	void feedModify(FeedModificationRequest feedModificationRequest);
	
	/*@Post("/feed/export")
	String feedExport();*/
	
	// Default values are readType=all newerThan=0 offset=0 limit=20 order=desc
	@Get("/feed/entries?id={id}&readType={readType}&newerThan={newerThan}&offset={offset}&limit={limit}&order={order}")
	Entries feedEntries(String id, String readType, long newerThan, int offset, int limit, String order);
	
	/*@Get("/feed/entriesAsFeed?id={id}")
	String feedEntriesAsFeed(String id);*/
	
	@Get("/feed/fetch?title={title}&url={url}")
	FeedInfo feedFetch(String title, String url);
	
	@Post("/feed/refresh")
	void feedRefresh(IDRequest idRequest);
	
	@Post("/feed/mark")
	void feedMark(MarkRequest markRequest);
	
	@Get("/feed/get/{id}")
	Feed feedGet(long id);
	
	@Post("/feed/unsubscribe")
	void feedUnsubscribe(IDRequest idRequest);
}