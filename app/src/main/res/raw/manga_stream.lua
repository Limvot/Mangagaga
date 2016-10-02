print 'Hello from Lua!!!! MangaStream woop woop'

--pageNo = 1
mangaListType = 'All'

function getMangaListTypes()
    titleList = { }
    titleList[0] = 'All'
    titleList['numTypes'] = 1
    return titleList
end

function setMangaListType(type)
    mangaListType = type
end

function getMangaListPage1()
    --pageNo = 1
    return getMangaListPage()
end

function getMangaListPreviousPage()
   --if pageNo > 1 then pageNo = pageNo -1 end
   return getMangaListPage()
end

function getMangaListNextPage()
   --pageNo = pageNo + 1
   return getMangaListPage()
end

function getMangaListPage()
    return getMangaList('http://mangastream.com/manga')
end

function getMangaList(url)
   print('About to getMangaList!')
   print(apiObj)
   path = download_cf(url)
   --path = apiObj:download(url)
   pageSource = apiObj:readFile(path)
   apiObj:note('LuaScript downloaded (for manga): ' .. path)
   daList = {}
   --<td><strong><a href="http://mangastream.com/manga/air_gear">Air Gear</a></strong></td>
   regex = '<td><strong><a href="(.-)">(.-)</a></strong></td>'
   apiObj:note('Manga List Regex: ' .. regex)
   beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex)
   index = 0
   while ending do
       daList[index] = {title = mangaTitle, url = mangaURL}
       beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex, ending+1)
       index = index + 1
   end
   daList['numManga'] = index
   return daList
end


function initManga(manga)
   -- temp in case we need to edit the url (did for kissmanga)
   mangaURL = manga['url']
   apiObj:note('Manga Path: ' .. mangaURL)
   path = download_cf(mangaURL)
   --path = apiObj:download(mangaURL)

   pageSource = apiObj:readFile(path)
   apiObj:note('LuaScript downloaded (for chapter): ' .. path)

   -- Set up manga description and other nicities
   -- IF WE HAD ONE
   manga['description'] = 'MangaStream does not provide descriptions'

   daList = {}

   --<td><a href="http://mangastream.com/r/air_gear/358/3139/1">358 - Trick 358</a></td>
   
   regex = '<td><a href="(.-)">(.-)</a></td>'
   apiObj:note('Chapter List Regex: ' .. regex)
   beginning, ending, chapterURL, chapterTitle = string.find(pageSource, regex)
   index = 0
   while ending do
       daList[index] = {title = chapterTitle, url = chapterURL, chapterSetUp = false}
       beginning, ending, chapterURL, chapterTitle = string.find(pageSource, regex, ending+1)
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
   return download_cf(chapter['pageList'][page]['url'])
   --return apiObj:download(chapter['pageList'][page]['url'])
end

function setUpChapter(manga, chapter)
       -- manga stream's chapter page is the first page of the chapter
       pageURL = chapter['url']
       apiObj:note('The chapter first page URL is: ' .. pageURL)
       -- http://mangastream.com/r/toriko/389/3706/2, we want the 3706 part
       chapterNumRegex = 'mangastream.com/r/.-/.-/(.-)/.-'
       _, _, thisChapterNum = string.find(pageURL, chapterNumRegex)
       nextPageChapterNum = thisChapterNum

       -- the next page regex
       --<li class="next"><a href="http://mangastream.com/r/toriko/389/3706/2">Next &rarr;</a></li>
       regex = '<li class="next"><a href="(.-)">.-</a></li>'

       -- the image regex
       --<img id="manga-page" src="http://img.mangastream.com/cdn/manga/98/3704/005.png"/></a>
       imageRegex = '" src="(.-)"'

       index = 0
       daList = {}
       ending = 0
       while ending and nextPageChapterNum == thisChapterNum do
           apiObj:note('pageURL: ' .. pageURL)
           pagePath = download_cf(pageURL)
           --pagePath = apiObj:download(pageURL)
           pageSource = apiObj:readFile(pagePath)
           -- get the image url
           _, _, pageImageURL = string.find(pageSource, imageRegex)
           apiObj:note('pageImageURL: ' .. pageImageURL)

           daList[index] = {url = pageImageURL}
           -- get the next page url
           _, ending, pageURL = string.find(pageSource, regex, 0)
           -- get the chapter number out of this next page regex so we know when to stop
           if ending then
               _, _, thisChapterNum = string.find(pageURL, chapterNumRegex)
           end 
           index = index + 1
       end
       daList['numPages'] = index
       chapter['pageList'] = daList
       chapter['chapterSetUp'] = true
       apiObj:note('set up chapter with ' .. index .. ' pages!')
end

