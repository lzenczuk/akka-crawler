package com.github.lzenczuk.akkacrawler.web.directives

import java.util.UUID

import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

/**
  * Created by dev on 14/12/16.
  */
trait SessionDirectives {

  val SessionTokenCookieName = "session-token"

  /**
    * Session directive providing session token from cookie or generate new one
    */
  def session: Directive1[String] =
    optionalCookie(SessionTokenCookieName)
      .map(sessionTokenCookie => sessionTokenCookie.map(_.value).getOrElse(UUID.randomUUID().toString))
      .flatMap(sessionToken => setCookie(HttpCookie(SessionTokenCookieName, value = sessionToken)).tmap(_ => sessionToken))
}

object SessionDirectives extends SessionDirectives
