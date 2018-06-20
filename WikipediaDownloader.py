import pywikibot
from pywikibot import pagegenerators

for year in range(2000,2017):
    site = pywikibot.Site()
    cat = pywikibot.Category(site,'Category:'+str(year)+'_horror_films')
    gen = pagegenerators.CategorizedPageGenerator(cat)
    i =0
    for page in gen:
        text = page.text
        i+=1
        print(text)
        text_file = open("pages\\" +str(year)+"Horror_"+str(i)+".html", "w",encoding="utf-8")
        text_file.write(page.text)
        text_file.close()
    #    createIndex(text)
dict = {}
# page = pywikibot.Page(site, u"The_Da_Vinci_Code_(film)")
# text = page.text
# print(text)




