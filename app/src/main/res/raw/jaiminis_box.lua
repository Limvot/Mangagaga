print "Hello from Lua!!!! Jamini's Box woop woop"

mangaListType = 'All'
manga_list_url = 'https://jaiminisbox.com/reader/directory'

--<div class="title"><a href="https://jaiminisbox.com/reader/series/bakuman/" title="Bakuman">Bakuman</a> <span class="meta"></span></div>
manga_list_regex = '<div class="title"><a href="(.-)" title=".-">(.-)</a>'

--<a class="gbutton fright" href="https://jaiminisbox.com/reader/directory/2/">Next »</a>
manga_list_next_page_regex = '>Last »»</a>.-<a class="gbutton fright" href="(.-)">Next »</a>'
--<div class="title"><a href="https://jaiminisbox.com/reader/read/black_clover/en/0/123/" title="Page 123: The commoner's trap">Page 123: The commoner's trap</a></div>
chapter_list_regex = '<div class="title"><a href="(.-)" title=".-">(.-)</a></div>'

--<a href="https://jaiminisbox.com/reader/read/black_clover/en/0/112/page/2" onClick="return nextPage();">
next_page_regex = '<a href="([^"]-)" onClick="return nextPage%(%);">'

-- http://mangastream.com/r/toriko/389/3706/2, we want the 3706 part
chapter_number_regex = 'jaiminisbox.com/reader/read/.-/.-/.-/(.-)/'

--<img class="open" src="https://images2-focus-opensocial.googleusercontent.com/gadgets/proxy?container=focus&refresh=604800&url=https://jaiminisbox.com/reader/content/comics/black_clover_55f3d2ec5caf2/112-0-royal-knights-selection-exam_592ff6a8f3d1b/m0001.jpg"/>
image_regex = '<img class="open" src=".-&url=(.-)"/>'
page_image_url_prefix = ''

function getMangaListTypes()
    titleList = { }
    titleList[0] = 'All'
    titleList['numTypes'] = 1
    return titleList
end

function getMangaListPage(type)
   print('About to getMangaList!')
   current_page_url = manga_list_url
   daList = {}
   index = 0
   repeat
       apiObj:note('DOWNLOADING MANGA LIST PAGE')
       path = download_cf(current_page_url)
       pageSource = apiObj:readFile(path)
       ending = -1
       repeat
           beginning, ending, mangaURL, mangaTitle = string.find(pageSource, manga_list_regex, ending+1)
           daList[index] = {title = mangaTitle, url = mangaURL}
           index = index + 1
       until not ending
       -- take off final nil entry
       index = index - 1
       -- get the next page if it exists
       _, _, current_page_url = string.find(pageSource, manga_list_next_page_regex)
   until not current_page_url
   daList['numManga'] = index
   return daList
end

function initManga(manga)
   apiObj:note('Manga Path: ' .. manga['url'])
   apiObj:note('DOWNLOADING MANGA CHAPTER LIST PAGE')
   path = download_cf(manga['url'])
   pageSource = apiObj:readFile(path)

   -- Set up manga description and other nicities
   -- IF WE HAD ONE
   manga['description'] = 'MangaStream does not provide descriptions'

   apiObj:note('Chapter List Regex: ' .. chapter_list_regex)
   daList = {}
   beginning, ending, chapterURL, chapterTitle = string.find(pageSource, chapter_list_regex)
   index = 0
   while ending do
       daList[index] = {title = chapterTitle, url = chapterURL, chapterSetUp = false}
       beginning, ending, chapterURL, chapterTitle = string.find(pageSource, chapter_list_regex, ending+1)
       index = index + 1
   end
   daList['numChapters'] = index
   manga['chapter_list'] = daList
end

function getMangaChapterList(manga)
    return manga['chapter_list']
end

function getMangaChapterNumPages(manga, chapter)
   if not chapter['chapterSetUp'] then
       setUpChapter(manga, chapter)
   end
   return chapter['pageList']['numPages']
end

function getMangaChapterPage(manga, chapter, page)
   if not chapter['chapterSetUp'] then
       setUpChapter(manga, chapter)
   end
   apiObj:note('DOWNLOADING MANGA REAL PAGE')
   return download_cf(chapter['pageList'][page]['url'])
end

function setUpChapter(manga, chapter)
       -- manga stream's chapter page is the first page of the chapter
       pageURL = chapter['url']
       apiObj:note('The chapter first page URL is: ' .. pageURL)
       _, _, thisChapterNum = string.find(pageURL, chapter_number_regex)
       nextPageChapterNum = thisChapterNum
       apiObj:note('The chapters number is : ' .. thisChapterNum)

       index = 0
       daList = {}
       ending = 0
       while ending and nextPageChapterNum == thisChapterNum do
           apiObj:note('DOWNLOADING MANGA CONTAINER PAGE')
           apiObj:note('pageURL: ' .. pageURL)
           pagePath = download_cf(pageURL)
           pageSource = apiObj:readFile(pagePath)
           -- get the image url
           _, _, pageImageURL = string.find(pageSource, image_regex)
           if page_image_url_prefix ~= '' then
               pageImageURL = page_image_url_prefix .. pageImageURL
           end
           apiObj:note('pageImageURL: ' .. pageImageURL)

           daList[index] = {url = pageImageURL}
           -- get the next page url
           _, ending, pageURL = string.find(pageSource, next_page_regex, 0)
           -- get the chapter number out of this next page regex so we know when to stop
           if ending then
               _, _, thisChapterNum = string.find(pageURL, chapter_number_regex)
           end 
           index = index + 1
       end
       daList['numPages'] = index
       chapter['pageList'] = daList
       chapter['chapterSetUp'] = true
       apiObj:note('set up chapter with ' .. index .. ' pages!')
end

