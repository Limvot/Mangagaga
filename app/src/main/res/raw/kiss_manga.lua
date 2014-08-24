print 'Hello from Lua!!!! Own file Woop Woop'
pageNo = 1
function getMangaListPage1()
   pageNo = 1
   return getMangaList('http://kissmanga.com/MangaList')
end

function getMangaListPreviousPage()
   if pageNo > 1 then pageNo = pageNo -1 end
   return getMangaList('http://kissmanga.com/MangaList?page=' .. pageNo)
end

function getMangaListNextPage()
   pageNo = pageNo + 1
   return getMangaList('http://kissmanga.com/MangaList?page=' .. pageNo)
end

function getMangaList(url)
   path = apiObj:download(url)
   pageSource = apiObj:readFile(path)
   apiObj:note('LuaScript downloaded (for manga): ' .. path)
   daList = {}
   regex = '</div>\'>.-<a href="/Manga/(.-)">(.-)</a>'
   apiObj:note('Manga List Regex: ' .. regex)
   beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex)
   index = 0
   while ending do
       print('URL: ' .. mangaURL .. ', Title: ' .. mangaTitle)
       daList[index] = {title = mangaTitle, url = mangaURL}
       beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex, ending+1)
       index = index + 1
   end
   daList['numManga'] = index
   return daList
end


function getMangaChapterList(manga)
   mangaURL = 'http://kissmanga.com/Manga' .. '/' .. manga['url']
   apiObj:note('Manga Path: ' .. mangaURL)
   path = apiObj:download(mangaURL)
   pageSource = apiObj:readFile(path)
   apiObj:note('LuaScript downloaded (for chapter): ' .. path)
   daList = {}
   regex = '<a +href="/Manga/' .. escapeRegexStr(manga['url']) .. '/(.-)?id.-".->(.-)</a>'
   apiObj:note('Chapter List Regex: ' .. regex)
   beginning, ending, chapterURL, chapterTitle = string.find(pageSource, regex)
   index = 0
   while ending do
       print('Chapter URL: ' .. chapterURL .. ', Chapter Title: ' .. chapterTitle)
       daList[index] = {title = chapterTitle, url = chapterURL, chapterSetUp = false}
       beginning, ending, chapterURL, chapterTitle = string.find(pageSource, regex, ending+1)
       index = index + 1
   end
   daList['numChapters'] = index
   return daList
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
   return apiObj:download(chapter['pageList'][page]['url'])
end


function setUpChapter(manga, chapter)
       pageURL = 'http://kissmanga.com/Manga' .. '/' .. manga['url'] .. '/' .. chapter['url']
       apiObj:note('The Page URL is: ' .. pageURL)
       path = apiObj:download(pageURL)
       apiObj:note('After download')
       pageSource = apiObj:readFile(path)
       regex = 'lstImages%.push%("(.-)"%);'
       apiObj:note('Page List Regex: ' .. regex)
       beginning, ending, pageURL = string.find(pageSource, regex)
       index = 0
       daList = {}
       while ending do
           print('Page URL: ' .. pageURL)
           daList[index] = {url = pageURL}
           beginning, ending, pageURL = string.find(pageSource, regex, ending+1)
           index = index + 1
       end
       daList['numPages'] = index
       chapter['pageList'] = daList
       chapter['chapterSetUp'] = true
end

