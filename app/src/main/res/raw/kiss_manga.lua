print 'Hello from Lua!!!! Own file Woop Woop'

pageNo = 1
mangaListType = 'All'

function getMangaListTypes()
    titleList = { }
    titleList[0] = 'All'
    titleList[1] = 'Most Popular'
    titleList[2] = 'Latest Update'
    titleList[3] = 'Newest'
    titleList['numTypes'] = 4
    allChar = '#ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    i = 1
    while i < (string.len(allChar) + 1) do
        titleList[titleList['numTypes'] + i - 1] = string.sub(allChar, i, i)
        i = i + 1
    end
    titleList['numTypes'] = titleList['numTypes'] + i
    return titleList
end

function setMangaListType(type)
    mangaListType = type
end

function getMangaListPage1()
    pageNo = 1
    return getMangaListPage()
end

function getMangaListPreviousPage()
   if pageNo > 1 then pageNo = pageNo -1 end
   return getMangaListPage()
end

function getMangaListNextPage()
   pageNo = pageNo + 1
   return getMangaListPage()
end

function getMangaListPage()
    if mangaListType == 'All' then
        return getMangaList('http://kissmanga.com/MangaList?page=' .. pageNo)
    elseif mangaListType == 'Most Popular' then
        return getMangaList('http://kissmanga.com/MangaList/MostPopular?page=' .. pageNo)
    elseif mangaListType == 'Latest Update' then
        return getMangaList('http://kissmanga.com/MangaList/LatestUpdate?page=' .. pageNo)
    elseif mangaListType == 'Newest' then
        return getMangaList('http://kissmanga.com/MangaList/Newest?page=' .. pageNo)
    elseif mangaListType == '#' then
        return getMangaList('http://kissmanga.com/MangaList?c=0&page=' .. pageNo)
    end

    return getMangaList('http://kissmanga.com/MangaList?c=' .. mangaListType .. '&page=' .. pageNo)
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
       daList[index] = {title = mangaTitle, url = mangaURL}
       beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex, ending+1)
       index = index + 1
   end
   daList['numManga'] = index
   return daList
end


function initManga(manga)
   mangaURL = 'http://kissmanga.com/Manga' .. '/' .. manga['url']
   apiObj:note('Manga Path: ' .. mangaURL)
   path = apiObj:download(mangaURL)
   pageSource = apiObj:readFile(path)
   apiObj:note('LuaScript downloaded (for chapter): ' .. path)

   -- Set up manga description and other nicities
   descriptionRegex = '<span class="info">Summary:</span>.-<p.->(.-)</p>'
   _, _, mangaDescription = string.find(pageSource, descriptionRegex)
   manga['description'] = mangaDescription

   daList = {}
   regex = '<a +href="/Manga/' .. escapeRegexStr(manga['url']) .. '/(.-)?id.-".->(.-)</a>'
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
           daList[index] = {url = pageURL}
           beginning, ending, pageURL = string.find(pageSource, regex, ending+1)
           index = index + 1
       end
       daList['numPages'] = index
       chapter['pageList'] = daList
       chapter['chapterSetUp'] = true
end

