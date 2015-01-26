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

