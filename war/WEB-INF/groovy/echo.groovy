import javax.servlet.http.Cookie

if (request.cookies) {
   def c = request.cookies[0]
   c.maxAge = 0
   response.addCookie(c)
} else {
   response.addCookie(new Cookie('mycookie', 'the value'))
}

forward 'echo.gtpl'
