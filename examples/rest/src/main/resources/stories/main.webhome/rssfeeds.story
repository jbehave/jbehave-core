{{velocity}}
{{translation key="xe.rss.feeds.description"/}}
#set($search = "[[Main.Search]]")
#set($tags = "[[Main.Tags]]")

* {{html}}<strong><a href="$xwiki.getURL('Main.WebRss', 'view', 'xpage=plain')"><img src="$xwiki.getSkinFile('icons/xwiki/rss-mini.png')" title="$services.localization.render('xe.rss.pages.modified')" alt=""/></a> <a href="$xwiki.getURL('Main.WebRss', 'view', 'xpage=plain')">$services.localization.render('xe.rss.global')</a></strong> $services.localization.render('xe.rss.global.description'){{/html}}
* {{html}}<strong><a href="$xwiki.getURL('Blog.GlobalBlogRss', 'view', 'xpage=plain')"><img src="$xwiki.getSkinFile('icons/xwiki/rss-mini.png')" title="$services.localization.render('xe.rss.blog.feed')" alt=""/></a> <a href="$xwiki.getURL('Blog.GlobalBlogRss', 'view', 'xpage=plain')">$services.localization.render('xe.rss.blog')</a></strong> $services.localization.render('xe.rss.blog.description'){{/html}}
* **[[{{translation key="xe.rss.search"/}}>>Main.Search]]** $services.localization.render('xe.rss.search.description', [$search])
* **[[{{translation key="xe.rss.tags"/}}>>Main.Tags]]** $services.localization.render('xe.rss.tags.description', [$tags])
{{/velocity}}