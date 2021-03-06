package com.twitter.finagle

/**
 * Codecs provide protocol encoding and decoding via netty pipelines
 * as well as a standard filter stack that are applied to services
 * from this codec.
 */

import java.net.{InetSocketAddress, SocketAddress}
import org.jboss.netty.channel.{ChannelPipelineFactory, ChannelPipeline}
import com.twitter.util.Future

/**
 * Superclass for all codecs.
 */
trait Codec[Req, Rep] {
  /**
   * The pipeline factory that implements the protocol.
   */
  def pipelineFactory: ChannelPipelineFactory

  /**
   * Prepare a newly-created (connected) Service endpoint.  It becomes
   * available once the returned Future is satisfied.
   * Used to allow codec modifications to the service at the bottom of the network stack.
   */
  def prepareService(underlying: Service[Req, Rep]): Future[Service[Req, Rep]] =
    Future.value(underlying)

  /**
   * Prepare a factory for usage with the codec.
   * Used to allow codec modifications to the service at the top of the network stack.
   */
  def prepareFactory(underlying: ServiceFactory[Req, Rep]): ServiceFactory[Req, Rep] =
    underlying
}

object Codec {
  def ofPipelineFactory[Req, Rep](makePipeline: => ChannelPipeline) = new Codec[Req, Rep] {
    def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline = makePipeline
    }
  }

  def ofPipeline[Req, Rep](p: ChannelPipeline) = new Codec[Req, Rep] {
    def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline = p
    }
  }
}

/**
 * Codec factories create codecs given some configuration.
 */


/**
 * Clients
 */
case class ClientCodecConfig(serviceName: String)


/**
 * Servers
 */
case class ServerCodecConfig(serviceName: String, boundAddress: SocketAddress) {
  def boundInetSocketAddress = boundAddress match {
    case ia: InetSocketAddress => ia
    case _ => new InetSocketAddress(0)
  }
}

/**
 * A combined codec factory provides both client and server codec
 * factories in one (when available).
 */
trait CodecFactory[Req, Rep] {
  type Client = ClientCodecConfig => Codec[Req, Rep]
  type Server = ServerCodecConfig => Codec[Req, Rep]

  def client: Client
  def server: Server
}
