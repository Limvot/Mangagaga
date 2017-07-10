print('Lua prequal!')

apiObj = 0
function init(apiObjIn)
   print('Doing init')
   print(apiObjIn)
   apiObj = apiObjIn
   print(apiObj)
end

function escapeRegexStr(theStr)
   print('Escaping ' .. theStr)
   newStr = (theStr:gsub('[%-%.%+%[%]%(%)%$$^%%%?%*]', '%%%1'):gsub('%z','%%z'))
   print('Done Escaping ' .. theStr .. ' as ' .. newStr)
   return newStr
end

function download_cf(str)
    print('Downloading with CloudFlare passthrough ' .. str)
    --result = apiObj:downloadWithRequestHeaders("http://mangagaga.room409.xyz/")
    result = apiObj:downloadWithRequestHeadersAndReferrer(str,'')
    print('result')
    print(result)
    print(result:getFirst())
    file_name = result:getFirst()
    response_headers = result:getSecond()
    print(file_name)
    print(response_headers)
    if response_headers:containsKey('X-Android-Response-Source') then
        if string.find(response_headers:get('X-Android-Response-Source'):get(0), escapeRegexStr('NETWORK 503'))
            and response_headers:containsKey('Server')
            and string.find(response_headers:get('Server'):get(0), escapeRegexStr('cloudflare-nginx')) then
            print('CLOUDFLARE DETECTED')
            page = apiObj:readFile(file_name)
            print(page)
            _,_,challenge = string.find(page, 'name="jschl_vc" value="(.-)"')
            print('found challenge: ')
            print(challenge)
            _,_,challenge_pass = string.find(page, 'name="pass" value="(.-)"')
            print('found challenge_pass: ')
            print(challenge_pass)
            _,_,to_eval = string.find(page, escapeRegexStr('setTimeout(function(){') .. '(.-)' .. escapeRegexStr('}, 4000);'))
            print('found to_eval: ')
            print(to_eval)
            to_eval = string.gsub(to_eval, '%s-t =.-;', '')
            to_eval = string.gsub(to_eval, '%s-a =.-;', '')
            to_eval = string.gsub(to_eval, '%s-f =.-;', '')
            to_eval = string.gsub(to_eval, '%s-r =.-;', '')
            to_eval = string.gsub(to_eval, '%s-t%.innerHTML.-;', '')
            to_eval = string.gsub(to_eval, '%s-f%.submit.-;', '')
            to_eval = string.gsub(to_eval, '%s-a%.value = (.-) %+ t.length;(.*)', '%1')
            print('edited to')
            print(to_eval)
            _,_,domain = string.find(str, 'http://(.-)/')
            print('domain is')
            print(domain)
            print('value is')
            answer = tonumber(apiObj:doDaJS(to_eval)) + string.len(domain)
            print(answer)
            _,_,protocol = string.find(str, '(.-)://')
            print('protocol')
            print(protocol)
            submit = protocol .. '://' .. domain .. '/cdn-cgi/l/chk_jschl'
                ..'?jschl_vc='..challenge..'&jschl_answer='..answer..'&pass='..challenge_pass
            print('submit')
            print(submit)
            print('waiting...')
            apiObj:sleep(5000)
            print('done')
            return apiObj:downloadWithRequestHeadersAndReferrer(submit,str):getFirst()
        else
            print('NO CLOUDFLARE - no string ')
            print(find_string)
            print(escapeRegexStr('URL=/cdn-cgi/'))
            print(find_result)
        end
    else
        print('NO CLOUDFLARE - no refresh')
    end

    return file_name
end

