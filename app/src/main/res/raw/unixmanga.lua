print 'Hello from Lua!!!! Own file Woop Woop'

pageNo = 1
mangaListType = 'All'

function getMangaListTypes()
    titleList = { }
    titleList[0] = 'All'
    --titleList[1] = 'Hot Manga'
    --titleList[2] = 'Latest Update'
    --titleList[3] = 'Newest'
    titleList['numTypes'] = 1
    allChar = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
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
        return getMangaList('http://unixmanga.nl/onlinereading/manga-lists.html')
    elseif mangaListType == 'Hot Manga' then
        return getMangaList('http://unixmanga.nl/onlinereading/manga-lists.html')
    elseif mangaListType == 'Latest Update' then
        return getMangaList('http://unixmanga.nl/onlinereading/0-desc-date.php')
    elseif mangaListType == 'Newest' then
        return getMangaList('http://unixmanga.nl/onlinereading/0-desc-date.php')
    elseif mangaListType == '#' then
        return getMangaList('http://unixmanga.nl/onlinereading/manga-lists.html')
    end

    return getMangaList('http://unixmanga.nl/onlinereading/manga-lists.html')
end

function getMangaList(url)
   print('About to getMangaList!')
   print(apiObj)
   num_per_page = 200
   path = download_cf(url)
   pageSource = apiObj:readFile(path)
   apiObj:note('LuaScript downloaded (for manga): ' .. path)
   daList = {}
   regex = '<td>.-<a href="http://unixmanga.nl/onlinereading/(.-)"title=".-">(.-)</a>.-</td>'
   apiObj:note('Manga List Regex: ' .. regex)
   beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex)
   index = 0
   daList[index] = {title = mangaTitle, url = mangaURL}
   count = 0

   if mangaListType == 'All'  then
    while count < num_per_page*(pageNo-1) do
        beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex, ending+1)
        count = count + 1
    end
    elseif mangaListType == 'Hot Manga' or mangaListType == 'Newest' then
        -- Do Stuff for pulling in latests/hottest
    elseif mangaListType == 'Latest Update' then
        -- Handle Latest update!
    else
        while mangaListType ~= mangaTitle:sub(1,1) and beginning ~= nil do
            apiObj:note('mangaListType: '..mangaListType)
            apiObj:note(mangaTitle.char(1))
            beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex, ending+1)
        end
   end
        
   while count < num_per_page*pageNo do
       daList[index] = {title = mangaTitle, url = mangaURL}
       beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex, ending+1)
       index = index + 1
       count = count + 1
   end
   daList['numManga'] = index
   return daList
end

function extractChapterList(mangaURL, urlminhtml)
   local mangaURL = mangaURL
   local urlminhtml = urlminhtml
   local path = download_cf(mangaURL)
   local pageSource = apiObj:readFile(path)

   local daList = {}
    -- this is a weird one; it's to make sure it isn't just urlminhtml.html ------------\/
   local regex = '<td>.-<a +href="(http://unixmanga.nl/onlinereading/.-/' .. urlminhtml .. '..-.html)"title=".-">(.-)</a>'
   local beginning, ending, chapterURL, chapterTitle = string.find(pageSource, regex)
   local index = 0
   while ending do
       if string.find(chapterURL, '%d+%-%d+.html') then
           newList = extractChapterList(chapterURL, urlminhtml)
           local newListIdx = 0
           while newListIdx < newList['numChapters'] do 
               local newOne = newList[newListIdx]
               newOne['title'] = chapterTitle .. ' | ' .. newOne['title']
               daList[index] = newList[newListIdx]
               index = index + 1
               newListIdx = newListIdx + 1
           end
       else
           chapterURL = string.gsub(chapterURL, '.html', '_nas.html')
           daList[index] = {title = chapterTitle, url = chapterURL, chapterSetUp = false}
           index = index + 1
       end
       beginning, ending, chapterURL, chapterTitle = string.find(pageSource, regex, ending+1)
   end
   daList['numChapters'] = index
   return daList
end

function initManga(manga)
   manga['description'] = manga['title']
   mangaURL = 'http://unixmanga.nl/onlinereading/' .. manga['url']
   urlminhtml = manga['url']
   urlminhtml = string.gsub(urlminhtml, ".html", "")
   urlminhtml = escapeRegexStr(urlminhtml)
   manga['chapter_list'] = extractChapterList(mangaURL, urlminhtml)
end

function getMangaChapterList(manga)
    return manga['chapter_list']
end


function getMangaChapterNumPages(manga, chapter)
   if not chapter['chapterSetUp'] then
       setUpChapter(chapter)
   end
   return chapter['pageList']['numPages']
end


function getMangaChapterPage(manga, chapter, page)
   if not chapter['chapterSetUp'] then
       setUpChapter(chapter)
   end
   return download_cf(chapter['pageList'][page]['url'])
end

function setUpChapter(chapter)
       apiObj:note(chapter['url'])
       pageURL = chapter['url']
       apiObj:note('SETTING UP CHAPTER!!!')
       apiObj:note('The Page URL is: ' .. pageURL)
       path = download_cf(pageURL)
       apiObj:note('THE PATH: ' .. path)
       apiObj:note('After download')
       pageSource = apiObj:readFile(path)
       regex = '<.-http://.-(unixmanga.net/.-)">.-<.->'
       apiObj:note('Page List Regex: ' .. regex)
       beginning, ending, pageURL = string.find(pageSource, regex)
       index = 0
       daList = {}
       while ending do
           pageURL = string.gsub(pageURL, ' ', '%%20')
           pageURL = string.gsub(pageURL, '&server=nas.html', '')
           pageURL = 'http://nas.' .. string.gsub(pageURL, '?image=', '')
           apiObj:note(pageURL)
           daList[index] = {url = pageURL}
           beginning, ending, pageURL = string.find(pageSource, regex, ending+1)
           index = index + 1
       end
       daList['numPages'] = index
       chapter['pageList'] = daList
       chapter['chapterSetUp'] = true
end

