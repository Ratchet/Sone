/*
 * FreenetSone - WebInterface.java - Copyright © 2010 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.CoreListener;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.L10nFilter;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.sone.notify.ListNotification;
import net.pterodactylus.sone.template.CollectionAccessor;
import net.pterodactylus.sone.template.CssClassNameFilter;
import net.pterodactylus.sone.template.GetPagePlugin;
import net.pterodactylus.sone.template.IdentityAccessor;
import net.pterodactylus.sone.template.NotificationManagerAccessor;
import net.pterodactylus.sone.template.PostAccessor;
import net.pterodactylus.sone.template.ReplyAccessor;
import net.pterodactylus.sone.template.RequestChangeFilter;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.template.SubstringFilter;
import net.pterodactylus.sone.web.ajax.CreateReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.DeletePostAjaxPage;
import net.pterodactylus.sone.web.ajax.DeleteReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.DismissNotificationAjaxPage;
import net.pterodactylus.sone.web.ajax.FollowSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.GetLikesAjaxPage;
import net.pterodactylus.sone.web.ajax.GetNotificationsAjaxPage;
import net.pterodactylus.sone.web.ajax.GetReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.GetSoneStatusPage;
import net.pterodactylus.sone.web.ajax.GetTranslationPage;
import net.pterodactylus.sone.web.ajax.LikeAjaxPage;
import net.pterodactylus.sone.web.ajax.LockSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.UnfollowSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.UnlikeAjaxPage;
import net.pterodactylus.sone.web.ajax.UnlockSoneAjaxPage;
import net.pterodactylus.sone.web.page.PageToadlet;
import net.pterodactylus.sone.web.page.PageToadletFactory;
import net.pterodactylus.sone.web.page.StaticPage;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.notify.NotificationManager;
import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.DateFilter;
import net.pterodactylus.util.template.DefaultTemplateFactory;
import net.pterodactylus.util.template.MatchFilter;
import net.pterodactylus.util.template.PaginationPlugin;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateException;
import net.pterodactylus.util.template.TemplateFactory;
import net.pterodactylus.util.template.TemplateProvider;
import net.pterodactylus.util.template.XmlFilter;
import net.pterodactylus.util.thread.Ticker;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContainer;
import freenet.l10n.BaseL10n;

/**
 * Bundles functionality that a web interface of a Freenet plugin needs, e.g.
 * references to l10n helpers.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WebInterface implements CoreListener {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(WebInterface.class);

	/** The notification manager. */
	private final NotificationManager notificationManager = new NotificationManager();

	/** The Sone plugin. */
	private final SonePlugin sonePlugin;

	/** The registered toadlets. */
	private final List<PageToadlet> pageToadlets = new ArrayList<PageToadlet>();

	/** The form password. */
	private final String formPassword;

	/** The template factory. */
	private DefaultTemplateFactory templateFactory;

	/** The “new Sone” notification. */
	private final ListNotification<Sone> newSoneNotification;

	/** The “new post” notification. */
	private final ListNotification<Post> newPostNotification;

	/** The “new reply” notification. */
	private final ListNotification<Reply> newReplyNotification;

	/**
	 * Creates a new web interface.
	 *
	 * @param sonePlugin
	 *            The Sone plugin
	 */
	public WebInterface(SonePlugin sonePlugin) {
		this.sonePlugin = sonePlugin;
		formPassword = sonePlugin.pluginRespirator().getToadletContainer().getFormPassword();

		templateFactory = new DefaultTemplateFactory();
		templateFactory.addAccessor(Object.class, new ReflectionAccessor());
		templateFactory.addAccessor(Collection.class, new CollectionAccessor());
		templateFactory.addAccessor(Sone.class, new SoneAccessor(getCore()));
		templateFactory.addAccessor(Post.class, new PostAccessor(getCore()));
		templateFactory.addAccessor(Reply.class, new ReplyAccessor(getCore()));
		templateFactory.addAccessor(Identity.class, new IdentityAccessor(getCore()));
		templateFactory.addAccessor(NotificationManager.class, new NotificationManagerAccessor());
		templateFactory.addFilter("date", new DateFilter());
		templateFactory.addFilter("l10n", new L10nFilter(getL10n()));
		templateFactory.addFilter("substring", new SubstringFilter());
		templateFactory.addFilter("xml", new XmlFilter());
		templateFactory.addFilter("change", new RequestChangeFilter());
		templateFactory.addFilter("match", new MatchFilter());
		templateFactory.addFilter("css", new CssClassNameFilter());
		templateFactory.addPlugin("getpage", new GetPagePlugin());
		templateFactory.addPlugin("paginate", new PaginationPlugin());
		templateFactory.setTemplateProvider(new ClassPathTemplateProvider(templateFactory));
		templateFactory.addTemplateObject("formPassword", formPassword);

		/* create notifications. */
		Template newSoneNotificationTemplate = templateFactory.createTemplate(createReader("/templates/notify/newSoneNotification.html"));
		newSoneNotification = new ListNotification<Sone>("new-sone-notification", "sones", newSoneNotificationTemplate);

		Template newPostNotificationTemplate = templateFactory.createTemplate(createReader("/templates/notify/newPostNotification.html"));
		newPostNotification = new ListNotification<Post>("new-post-notification", "posts", newPostNotificationTemplate);

		Template newReplyNotificationTemplate = templateFactory.createTemplate(createReader("/templates/notify/newReplyNotification.html"));
		newReplyNotification = new ListNotification<Reply>("new-replies-notification", "replies", newReplyNotificationTemplate);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone core used by the Sone plugin.
	 *
	 * @return The Sone core
	 */
	public Core getCore() {
		return sonePlugin.core();
	}

	/**
	 * Returns the notification manager.
	 *
	 * @return The notification manager
	 */
	public NotificationManager getNotifications() {
		return notificationManager;
	}

	/**
	 * Returns the l10n helper of the node.
	 *
	 * @return The node’s l10n helper
	 */
	public BaseL10n getL10n() {
		return sonePlugin.l10n().getBase();
	}

	/**
	 * Returns the session manager of the node.
	 *
	 * @return The node’s session manager
	 */
	public SessionManager getSessionManager() {
		try {
			return sonePlugin.pluginRespirator().getSessionManager(new URI("/"));
		} catch (URISyntaxException use1) {
			logger.log(Level.SEVERE, "Could not get Session Manager!", use1);
			return null;
		}
	}

	/**
	 * Returns the node’s form password.
	 *
	 * @return The form password
	 */
	public String getFormPassword() {
		return formPassword;
	}

	//
	// ACTIONS
	//

	/**
	 * Starts the web interface and registers all toadlets.
	 */
	public void start() {
		registerToadlets();

		/* notification templates. */
		Template startupNotificationTemplate = templateFactory.createTemplate(createReader("/templates/notify/startupNotification.html"));

		final TemplateNotification startupNotification = new TemplateNotification("startup-notification", startupNotificationTemplate);
		notificationManager.addNotification(startupNotification);

		Ticker.getInstance().registerEvent(System.currentTimeMillis() + (120 * 1000), new Runnable() {

			@Override
			public void run() {
				startupNotification.dismiss();
			}
		}, "Sone Startup Notification Remover");

		Template wotMissingNotificationTemplate = templateFactory.createTemplate(createReader("/templates/notify/wotMissingNotification.html"));
		final TemplateNotification wotMissingNotification = new TemplateNotification("wot-missing-notification", wotMissingNotificationTemplate);
		Ticker.getInstance().registerEvent(System.currentTimeMillis() + (15 * 1000), new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				if (getCore().getIdentityManager().isConnected()) {
					wotMissingNotification.dismiss();
				} else {
					notificationManager.addNotification(wotMissingNotification);
				}
				Ticker.getInstance().registerEvent(System.currentTimeMillis() + (15 * 1000), this, "Sone WoT Connector Checker");
			}

		}, "Sone WoT Connector Checker");
	}

	/**
	 * Stops the web interface and unregisters all toadlets.
	 */
	public void stop() {
		unregisterToadlets();
		Ticker.getInstance().stop();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Register all toadlets.
	 */
	private void registerToadlets() {
		Template loginTemplate = templateFactory.createTemplate(createReader("/templates/login.html"));
		Template indexTemplate = templateFactory.createTemplate(createReader("/templates/index.html"));
		Template knownSonesTemplate = templateFactory.createTemplate(createReader("/templates/knownSones.html"));
		Template createSoneTemplate = templateFactory.createTemplate(createReader("/templates/createSone.html"));
		Template createPostTemplate = templateFactory.createTemplate(createReader("/templates/createPost.html"));
		Template createReplyTemplate = templateFactory.createTemplate(createReader("/templates/createReply.html"));
		Template editProfileTemplate = templateFactory.createTemplate(createReader("/templates/editProfile.html"));
		Template viewSoneTemplate = templateFactory.createTemplate(createReader("/templates/viewSone.html"));
		Template viewPostTemplate = templateFactory.createTemplate(createReader("/templates/viewPost.html"));
		Template likePostTemplate = templateFactory.createTemplate(createReader("/templates/like.html"));
		Template unlikePostTemplate = templateFactory.createTemplate(createReader("/templates/unlike.html"));
		Template deletePostTemplate = templateFactory.createTemplate(createReader("/templates/deletePost.html"));
		Template deleteReplyTemplate = templateFactory.createTemplate(createReader("/templates/deleteReply.html"));
		Template lockSoneTemplate = templateFactory.createTemplate(createReader("/templates/lockSone.html"));
		Template unlockSoneTemplate = templateFactory.createTemplate(createReader("/templates/unlockSone.html"));
		Template followSoneTemplate = templateFactory.createTemplate(createReader("/templates/followSone.html"));
		Template unfollowSoneTemplate = templateFactory.createTemplate(createReader("/templates/unfollowSone.html"));
		Template deleteSoneTemplate = templateFactory.createTemplate(createReader("/templates/deleteSone.html"));
		Template noPermissionTemplate = templateFactory.createTemplate(createReader("/templates/noPermission.html"));
		Template dismissNotificationTemplate = templateFactory.createTemplate(createReader("/templates/dismissNotification.html"));
		Template logoutTemplate = templateFactory.createTemplate(createReader("/templates/logout.html"));
		Template optionsTemplate = templateFactory.createTemplate(createReader("/templates/options.html"));
		Template aboutTemplate = templateFactory.createTemplate(createReader("/templates/about.html"));
		Template replyTemplate = templateFactory.createTemplate(createReader("/templates/include/viewReply.html"));

		PageToadletFactory pageToadletFactory = new PageToadletFactory(sonePlugin.pluginRespirator().getHLSimpleClient(), "/Sone/");
		pageToadlets.add(pageToadletFactory.createPageToadlet(new IndexPage(indexTemplate, this), "Index"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreateSonePage(createSoneTemplate, this), "CreateSone"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new KnownSonesPage(knownSonesTemplate, this), "KnownSones"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditProfilePage(editProfileTemplate, this), "EditProfile"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreatePostPage(createPostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreateReplyPage(createReplyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new ViewSonePage(viewSoneTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new ViewPostPage(viewPostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LikePage(likePostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnlikePage(unlikePostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeletePostPage(deletePostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteReplyPage(deleteReplyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LockSonePage(lockSoneTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnlockSonePage(unlockSoneTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new FollowSonePage(followSoneTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnfollowSonePage(unfollowSoneTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteSonePage(deleteSoneTemplate, this), "DeleteSone"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LoginPage(loginTemplate, this), "Login"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LogoutPage(logoutTemplate, this), "Logout"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new OptionsPage(optionsTemplate, this), "Options"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new AboutPage(aboutTemplate, this, SonePlugin.VERSION), "About"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new SoneTemplatePage("noPermission.html", noPermissionTemplate, "Page.NoPermission.Title", this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DismissNotificationPage(dismissNotificationTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage("css/", "/static/css/", "text/css")));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage("javascript/", "/static/javascript/", "text/javascript")));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage("images/", "/static/images/", "image/png")));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetTranslationPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetNotificationsAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DismissNotificationAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetSoneStatusPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreateReplyAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetReplyAjaxPage(this, replyTemplate)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeletePostAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteReplyAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LockSoneAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnlockSoneAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new FollowSoneAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnfollowSoneAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LikeAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnlikeAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetLikesAjaxPage(this)));

		ToadletContainer toadletContainer = sonePlugin.pluginRespirator().getToadletContainer();
		toadletContainer.getPageMaker().addNavigationCategory("/Sone/index.html", "Navigation.Menu.Name", "Navigation.Menu.Tooltip", sonePlugin);
		for (PageToadlet toadlet : pageToadlets) {
			String menuName = toadlet.getMenuName();
			if (menuName != null) {
				toadletContainer.register(toadlet, "Navigation.Menu.Name", toadlet.path(), true, "Navigation.Menu.Item." + menuName + ".Name", "Navigation.Menu.Item." + menuName + ".Tooltip", false, toadlet);
			} else {
				toadletContainer.register(toadlet, null, toadlet.path(), true, false);
			}
		}
	}

	/**
	 * Unregisters all toadlets.
	 */
	private void unregisterToadlets() {
		ToadletContainer toadletContainer = sonePlugin.pluginRespirator().getToadletContainer();
		for (PageToadlet pageToadlet : pageToadlets) {
			toadletContainer.unregister(pageToadlet);
		}
		toadletContainer.getPageMaker().removeNavigationCategory("Navigation.Menu.Name");
	}

	/**
	 * Creates a {@link Reader} from the {@link InputStream} for the resource
	 * with the given name.
	 *
	 * @param resourceName
	 *            The name of the resource
	 * @return A {@link Reader} for the resource
	 */
	private Reader createReader(String resourceName) {
		try {
			return new InputStreamReader(getClass().getResourceAsStream(resourceName), "UTF-8");
		} catch (UnsupportedEncodingException uee1) {
			return null;
		}
	}

	//
	// CORELISTENER METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void newSoneFound(Sone sone) {
		newSoneNotification.add(sone);
		notificationManager.addNotification(newSoneNotification);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void newPostFound(Post post) {
		newPostNotification.add(post);
		notificationManager.addNotification(newPostNotification);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void newReplyFound(Reply reply) {
		if (reply.getPost().getSone() == null) {
			return;
		}
		newReplyNotification.add(reply);
		notificationManager.addNotification(newReplyNotification);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markSoneKnown(Sone sone) {
		newSoneNotification.remove(sone);
		if (newSoneNotification.isEmpty()) {
			newSoneNotification.dismiss();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markPostKnown(Post post) {
		newPostNotification.remove(post);
		if (newPostNotification.isEmpty()) {
			newPostNotification.dismiss();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markReplyKnown(Reply reply) {
		newReplyNotification.remove(reply);
		if (newReplyNotification.isEmpty()) {
			newReplyNotification.dismiss();
		}
	}

	/**
	 * Template provider implementation that uses
	 * {@link WebInterface#createReader(String)} to load templates for
	 * inclusion.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private class ClassPathTemplateProvider implements TemplateProvider {

		/** The template factory. */
		@SuppressWarnings("hiding")
		private final TemplateFactory templateFactory;

		/**
		 * Creates a new template provider that locates templates on the
		 * classpath.
		 *
		 * @param templateFactory
		 *            The template factory to create the templates
		 */
		public ClassPathTemplateProvider(TemplateFactory templateFactory) {
			this.templateFactory = templateFactory;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public Template getTemplate(String templateName) {
			Reader templateReader = createReader("/templates/" + templateName);
			if (templateReader == null) {
				return null;
			}
			Template template = templateFactory.createTemplate(templateReader);
			try {
				template.parse();
			} catch (TemplateException te1) {
				logger.log(Level.WARNING, "Could not parse template “" + templateName + "” for inclusion!", te1);
			}
			return template;
		}

	}

}
