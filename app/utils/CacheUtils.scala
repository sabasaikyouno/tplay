package utils

import domain.repository.RoomDataRepository
import models.room.RoomData
import play.api.cache.SyncCacheApi

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CacheUtils {

  def getRoom(roomId: String)(implicit cache: SyncCacheApi, roomDataRepository: RoomDataRepository) =
    cache.get[RoomData](roomId).fold(roomDataRepository.getOneRoom(roomId))(v => Future(Some(v)))

  def getAuthUsers(roomId: String)(implicit cache: SyncCacheApi, roomDataRepository: RoomDataRepository) =
    cache.get[List[String]](roomId+"authUsers").fold(roomDataRepository.getAuthUsers(roomId))(Future(_))

  // 値がNoneの場合セットしない。
  def setOptCache[A](key: String, valueOpt: Option[A])(implicit cache: SyncCacheApi) =
    valueOpt.foreach(cache.set(key, _))
}
