package com.github.lzenczuk.akkacrawler.models.httpclient

/**
  * Created by dev on 15/12/16.
  */

/**
  * Model for http request
  * @param method - "GET", "POST", "PUT"
  * @param url - url to fetch
  */
case class CHttpRequest(method: String, url: String)

object CHttpResponse {
  def apply(error: String): CHttpResponse = CHttpErrorResponse(error)
  def apply(status: Int, statusMessage: String): CHttpResponse = CHttpSuccessResponse(status, statusMessage)
}

trait CHttpResponse {
  def isRedirection: Boolean
  def isSuccess:Boolean
  def isError:Boolean
}

case class CHttpErrorResponse(error: String) extends CHttpResponse {
  override def isRedirection: Boolean = false
  override def isError: Boolean = true
  override def isSuccess: Boolean = false
}

case class CHttpSuccessResponse(status: Int, statusMessage: String) extends CHttpResponse {
  override def isRedirection: Boolean = 300 to 399 contains status
  override def isError: Boolean = false
  override def isSuccess: Boolean = true
}

case class CHttpRequestResponse(req:CHttpRequest, resp:CHttpResponse)

