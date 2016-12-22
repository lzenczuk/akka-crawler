package com.github.lzenczuk.akkacrawler.service.cluster

/**
  * Created by dev on 22/12/16.
  */
trait ClusterService {
  def createCluster():Unit
  def leaveCluster():Unit
  def joinCluster(system:String, host:String, port:Int):Unit
}


