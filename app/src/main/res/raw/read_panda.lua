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
        return getMangaList('http://readpanda.net/manga-list/' .. pageNo)
    elseif mangaListType == 'Latest Update' then
        return getMangaList('http://readpanda.net/latest-releases/' .. pageNo)
    end

    return getMangaList('http://readpanda.net/manga-list/' .. pageNo)
end
    --if mangaListType == 'All' then
        --return getMangaList('http://readpanda.net/manga-list/' .. pageNo)
    --elseif mangaListType == 'Most Popular' then
        --return getMangaList('http://kissmanga.com/MangaList/MostPopular?page=' .. pageNo)
    --elseif mangaListType == 'Latest Update' then
        --return getMangaList('http://kissmanga.com/MangaList/LatestUpdate?page=' .. pageNo)
    --elseif mangaListType == 'Newest' then
        --return getMangaList('http://kissmanga.com/MangaList/Newest?page=' .. pageNo)
    --elseif mangaListType == '#' then
        --return getMangaList('http://kissmanga.com/MangaList?c=0&page=' .. pageNo)
    --end

    --return getMangaList('http://kissmanga.com/MangaList?c=' .. mangaListType .. '&page=' .. pageNo)
--end

function getMangaList(url)
   print('About to getMangaList!')
   print(apiObj)
   path = apiObj:download(url)
   pageSource = apiObj:readFile(path)
   apiObj:note('LuaScript downloaded (for manga): ' .. path)
   daList = {}
   regex = '<a href="(.-)" title="(.-)".-</a>'
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
   mangaURL = manga['url']
   apiObj:note('Manga Path: ' .. mangaURL)
   path = apiObj:download(mangaURL)
   pageSource = apiObj:readFile(path)
   apiObj:note('LuaScript downloaded (for chapter): ' .. path)

   -- Set up manga description and other nicities
   descriptionRegex = '<span class="info">Summary:</span>.-<p.->(.-)</p>'
   _, _, mangaDescription = string.find(pageSource, descriptionRegex)
   manga['description'] = mangaDescription

   daList = {}
   regex = '<li>.-<a href="(.-)">(.-)</a>:(.-)<span class=.-</li>'
   apiObj:note('Chapter List Regex: ' .. regex)
   beginning, ending, chapterURL, chapterTitle, titleDiscripton = string.find(pageSource, regex)
   index = 0
   while ending do
       daList[index] = {title = chapterTitle .. titleDiscripton, url = chapterURL, chapterSetUp = false}
       beginning, ending, chapterURL, chapterTitle, titleDiscripton = string.find(pageSource, regex, ending+1)
       index = index + 1
   end
   daList['numChapters'] = index
   manga['chapter_list'] = daList
end

function getMangaChapterList(manga)
    return manga['chapter_list']
end


function getMangaChapterNumPages(manga, chapter)
    apiObj:note('Getting pages for chapter  ' .. chapter['title'])
   if not chapter['chapterSetUp'] then
       setUpChapter(manga, chapter)
   end
   return chapter['pageList']['numPages']
end


function getMangaChapterPage(manga, chapter, page)
   if not chapter['chapterSetUp'] then
       setUpChapter(manga, chapter)
       apiObj:note('Chapter ' .. chapter['title'] .. ' was NOT setup.')
   else
       apiObj:note('Chapter ' .. chapter['title'] .. ' WAS not setup.')
   end

   regex = 'top.-img src="(.-)" alt='
   pageURL = chapter['pageList'][page]['htmlPageUrl']

   apiObj:note('The Page URL for page ' .. page .. ' (for actuall image grabbing) is: ' .. pageURL)
   path = apiObj:download(pageURL)
   apiObj:note('After download')
   pageSource = apiObj:readFile(path)
   beginning, ending, pageURL = string.find(pageSource, regex)
   apiObj:note('image URL: ' .. pageURL)
   return apiObj:download(pageURL)
end


function setUpChapter(manga, chapter)
       pageURL = chapter['url']
       apiObj:note('The chapter URL is: ' .. pageURL)
       path = apiObj:download(pageURL)
       apiObj:note('After download')
       pageSource = apiObj:readFile(path)
       -- pull out the number of the last page
       count_regex = '<select class="cbo_wpm_pag".*<option value=".-" >(.-)</option></select>'
       apiObj:note('Count Regex: ' .. count_regex)
       beginning, ending, countStr = string.find(pageSource, count_regex)
       apiObj:note('countStr: ' .. countStr)
       numPages = tonumber(countStr)
       apiObj:note('num pages: ' .. numPages)

       index = 0
       daList = {}
       apiObj:note('Page List Regex: ' .. regex)
       while index < numPages do
           pageURL = chapter['url'] .. (index+1)
           daList[index] = {htmlPageUrl = pageURL}
           index = index + 1
       end
       daList['numPages'] = numPages
       chapter['pageList'] = daList
       chapter['chapterSetUp'] = true
end

