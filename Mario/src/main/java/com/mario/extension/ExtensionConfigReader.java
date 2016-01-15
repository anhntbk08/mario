package com.mario.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mario.config.CassandraConfig;
import com.mario.config.HazelcastConfig;
import com.mario.config.HttpMessageProducerConfig;
import com.mario.config.LifeCycleConfig;
import com.mario.config.ManagedObjectConfig;
import com.mario.config.MessageHandlerConfig;
import com.mario.config.MessageProducerConfig;
import com.mario.config.MonitorAgentConfig;
import com.mario.config.RabbitMQProducerConfig;
import com.mario.config.RedisConfig;
import com.mario.config.WorkerPoolConfig;
import com.mario.config.gateway.GatewayConfig;
import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.HttpGatewayConfig;
import com.mario.config.gateway.RabbitMQGatewayConfig;
import com.mario.config.gateway.SocketGatewayConfig;
import com.mario.config.serverwrapper.HttpServerWrapperConfig;
import com.mario.config.serverwrapper.RabbitMQServerWrapperConfig;
import com.mario.config.serverwrapper.ServerWrapperConfig;
import com.mario.config.serverwrapper.ServerWrapperConfig.ServerWrapperType;
import com.mario.extension.xml.EndpointReader;
import com.mario.gateway.http.JettyHttpServerOptions;
import com.mario.gateway.socket.SocketProtocol;
import com.nhb.common.data.PuObject;
import com.nhb.common.db.cassandra.CassandraDatasourceConfig;
import com.nhb.common.db.mongodb.config.MongoDBConfig;
import com.nhb.common.db.mongodb.config.MongoDBCredentialConfig;
import com.nhb.common.db.sql.SQLDataSourceConfig;
import com.nhb.common.exception.UnsupportedTypeException;
import com.nhb.common.vo.HostAndPort;
import com.nhb.common.vo.UserNameAndPassword;
import com.nhb.messaging.MessagingModel;
import com.nhb.messaging.http.HttpMethod;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;

class ExtensionConfigReader extends XmlConfigReader {

	private String extensionName;

	private List<LifeCycleConfig> lifeCycleConfigs;
	private List<GatewayConfig> gatewayConfigs;
	private List<SQLDataSourceConfig> sqlDatasourceConfigs;
	private List<HazelcastConfig> hazelcastConfigs;
	private List<RedisConfig> redisConfigs;
	private List<MongoDBConfig> mongoDBConfigs;
	private List<ServerWrapperConfig> serverWrapperConfigs;
	private List<MonitorAgentConfig> monitorAgentConfigs;
	private List<MessageProducerConfig> producerConfigs;

	private Collection<CassandraConfig> cassandraConfigs;

	@Override
	protected void read(Document document) throws Exception {
		this.extensionName = ((Node) xPath.compile("/mario/name").evaluate(document, XPathConstants.NODE))
				.getTextContent();
		if (extensionName == null || extensionName.trim().length() == 0) {
			throw new RuntimeException("extension cannot be empty");
		}

		try {
			this.readDataSourceConfigs(
					(Node) xPath.compile("/mario/datasources").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			this.readServerWrapperConfigs(
					(Node) xPath.compile("/mario/servers").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			this.readGatewayConfigs((Node) xPath.compile("/mario/gateways").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			this.readLifeCycleConfigs(
					(Node) xPath.compile("/mario/lifecycles").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			this.readMonitorAgentConfigs(
					(Node) xPath.compile("/mario/monitor").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			this.readProducerConfigs((Node) xPath.compile("/mario/producers").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}
	}

	private void readServerWrapperConfigs(Node node) throws XPathExpressionException {
		this.serverWrapperConfigs = new ArrayList<>();
		if (node == null) {
			return;
		}
		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeType() == 1) {
				ServerWrapperType connectionType = ServerWrapperType.fromName(item.getNodeName());
				switch (connectionType) {
				case HTTP: {
					HttpServerWrapperConfig httpServerWrapperConfig = new HttpServerWrapperConfig();
					Node curr = item.getFirstChild();
					while (curr != null) {
						if (curr.getNodeType() == 1) {
							switch (curr.getNodeName().trim().toLowerCase()) {
							case "name":
								httpServerWrapperConfig.setName(curr.getTextContent());
								break;
							case "port":
								httpServerWrapperConfig.setPort(Integer.valueOf(curr.getTextContent().trim()));
								break;
							case "options":
								httpServerWrapperConfig.setOptions(
										JettyHttpServerOptions.fromName(curr.getTextContent().trim()).getCode());
								break;
							case "sessiontimeout":
								httpServerWrapperConfig
										.setSessionTimeout(Integer.valueOf(curr.getTextContent().trim()));
								break;
							}
						}
						curr = curr.getNextSibling();
					}
					this.serverWrapperConfigs.add(httpServerWrapperConfig);
					break;
				}
				case RABBITMQ: {
					RabbitMQServerWrapperConfig rabbitMQServerWrapperConfig = new RabbitMQServerWrapperConfig();
					NodeList endpoints = (NodeList) xPath.compile("endpoint/entry").evaluate(item,
							XPathConstants.NODESET);
					for (int j = 0; j < endpoints.getLength(); j++) {
						Node endpointNode = endpoints.item(j);
						String host = null;
						int port = -1;
						try {
							host = ((Node) xPath.compile("host").evaluate(endpointNode, XPathConstants.NODE))
									.getTextContent();
						} catch (Exception ex) {
							getLogger().warn("host config is invalid : " + endpointNode.getTextContent(), ex);
						}
						try {
							port = Integer
									.valueOf(((Node) xPath.compile("port").evaluate(endpointNode, XPathConstants.NODE))
											.getTextContent());
						} catch (Exception ex) {
							getLogger().warn("port config is invalid : " + endpointNode.getTextContent(), ex);
						}
						if (host != null && port > 0) {
							HostAndPort endpoint = new HostAndPort(host, port);
							rabbitMQServerWrapperConfig.addEndpoint(endpoint);
						}
					}
					String userName = null;
					String password = null;
					try {
						userName = ((Node) xPath.compile("credential/username").evaluate(item, XPathConstants.NODE))
								.getTextContent();
					} catch (Exception e) {
						// do nothing
					}
					try {
						password = ((Node) xPath.compile("credential/password").evaluate(item, XPathConstants.NODE))
								.getTextContent();
					} catch (Exception e) {
						// do nothing
					}

					rabbitMQServerWrapperConfig.setName(
							((Node) xPath.compile("name").evaluate(item, XPathConstants.NODE)).getTextContent());
					rabbitMQServerWrapperConfig.setCredential(new UserNameAndPassword(userName, password));
					this.serverWrapperConfigs.add(rabbitMQServerWrapperConfig);
					break;
				}
				default:
					getLogger().warn("Connection type not supported: " + connectionType);
				}
			}
			item = item.getNextSibling();
		}
	}

	private WorkerPoolConfig readWorkerPoolConfig(Node node) throws XPathExpressionException {
		WorkerPoolConfig workerPoolConfig = null;
		if (node != null) {
			workerPoolConfig = new WorkerPoolConfig();
			Node element = node.getFirstChild();
			while (element != null) {
				if (element.getNodeType() == 1) {
					String value = element.getTextContent().trim();
					String nodeName = element.getNodeName();
					if (nodeName.equalsIgnoreCase("poolsize")) {
						workerPoolConfig.setPoolSize(Integer.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("ringbuffersize")) {
						workerPoolConfig.setRingBufferSize(Integer.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("threadnamepattern")) {
						workerPoolConfig.setThreadNamePattern(value);
					}
				}
				element = element.getNextSibling();
			}
		}
		return workerPoolConfig;
	}

	private RabbitMQQueueConfig readRabbitMQQueueConfig(Node node) {
		RabbitMQQueueConfig queueConfig = null;
		if (node != null) {
			queueConfig = new RabbitMQQueueConfig();
			Node element = node.getFirstChild();
			while (element != null) {
				if (element.getNodeType() == 1) {
					String nodeName = element.getNodeName();
					String value = element.getTextContent().trim();
					if (nodeName.equalsIgnoreCase("name") || nodeName.equalsIgnoreCase("queuename")) {
						queueConfig.setQueueName(value);
					} else if (nodeName.equalsIgnoreCase("autoack")) {
						queueConfig.setAutoAck(Boolean.valueOf(element.getTextContent()));
					} else if (nodeName.equalsIgnoreCase("exchangename")) {
						queueConfig.setExchangeName(value);
					} else if (nodeName.equalsIgnoreCase("exchangetype")) {
						queueConfig.setExchangeType(value);
					} else if (nodeName.equalsIgnoreCase("routingkey")) {
						queueConfig.setRoutingKey(value);
					} else if (nodeName.equalsIgnoreCase("type") || nodeName.equalsIgnoreCase("messagingmodel")) {
						queueConfig.setType(MessagingModel.fromName(value));
					} else if (nodeName.equalsIgnoreCase("qos")) {
						queueConfig.setQos(Integer.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("durable")) {
						queueConfig.setDurable(Boolean.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("exclusive")) {
						queueConfig.setExclusive(Boolean.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("autoDelete")) {
						queueConfig.setAutoDelete(Boolean.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("variables") || nodeName.equalsIgnoreCase("arguments")) {
						queueConfig.setArguments(PuObject.fromXML(element).toMap());
					}
				}
				element = element.getNextSibling();
			}
		}
		return queueConfig;
	}

	private void readGatewayConfigs(Node node) throws XPathExpressionException {
		this.gatewayConfigs = new ArrayList<GatewayConfig>();
		NodeList list = (NodeList) xPath.compile("*").evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			GatewayType type = GatewayType.fromName(item.getNodeName());
			if (type != null) {
				GatewayConfig config = null;
				Node ele = null;
				switch (type) {
				case HTTP:
					HttpGatewayConfig httpGatewayConfig = new HttpGatewayConfig();
					ele = item.getFirstChild();
					while (ele != null) {
						if (ele.getNodeType() == 1) {
							String value = ele.getTextContent().trim();
							String nodeName = ele.getNodeName();
							if (nodeName.equalsIgnoreCase("deserializer")) {
								httpGatewayConfig.setDeserializerClassName(value);
							} else if (nodeName.equalsIgnoreCase("serializer")) {
								httpGatewayConfig.setSerializerClassName(value);
							} else if (nodeName.equalsIgnoreCase("name")) {
								httpGatewayConfig.setName(value);
							} else if (nodeName.equalsIgnoreCase("workerpool")) {
								httpGatewayConfig.setWorkerPoolConfig(readWorkerPoolConfig(ele));
							} else if (nodeName.equalsIgnoreCase("path") || nodeName.equalsIgnoreCase("location")) {
								httpGatewayConfig.setPath(value);
							} else if (nodeName.equalsIgnoreCase("async")) {
								httpGatewayConfig.setAsync(Boolean.valueOf(value));
							} else if (nodeName.equalsIgnoreCase("encoding")) {
								httpGatewayConfig.setEncoding(value);
							} else if (nodeName.equalsIgnoreCase("server")) {
								httpGatewayConfig.setServerWrapperName(value);
							} else if (nodeName.equalsIgnoreCase("usemultipath")
									|| nodeName.equalsIgnoreCase("usingmultipath")) {
								httpGatewayConfig.setUseMultipath(Boolean.valueOf(value));
							} else if (nodeName.equalsIgnoreCase("header")) {
								String key = ele.getAttributes().getNamedItem("name").getNodeValue();
								if (key != null && key.trim().length() > 0) {
									httpGatewayConfig.getHeaders().put(key.trim(), value);
								}
							}
						}
						ele = ele.getNextSibling();
					}
					config = httpGatewayConfig;
					break;
				case RABBITMQ:
					RabbitMQGatewayConfig rabbitMQConfig = new RabbitMQGatewayConfig();
					ele = item.getFirstChild();
					while (ele != null) {
						if (ele.getNodeType() == 1) {
							String value = ele.getTextContent().trim();
							if (ele.getNodeName().equalsIgnoreCase("deserializer")) {
								rabbitMQConfig.setDeserializerClassName(value);
							} else if (ele.getNodeName().equalsIgnoreCase("serializer")) {
								rabbitMQConfig.setSerializerClassName(value);
							} else if (ele.getNodeName().equalsIgnoreCase("name")) {
								rabbitMQConfig.setName(value);
							} else if (ele.getNodeName().equalsIgnoreCase("workerpool")) {
								rabbitMQConfig.setWorkerPoolConfig(readWorkerPoolConfig(ele));
							} else if (ele.getNodeName().equalsIgnoreCase("server")) {
								rabbitMQConfig.setServerWrapperName(value);
							} else if (ele.getNodeName().equalsIgnoreCase("queue")) {
								rabbitMQConfig.setQueueConfig(readRabbitMQQueueConfig(ele));
							}
						}
						ele = ele.getNextSibling();
					}
					config = rabbitMQConfig;
					break;
				case SOCKET:
					SocketGatewayConfig socketGatewayConfig = new SocketGatewayConfig();
					ele = item.getFirstChild();
					while (ele != null) {
						if (ele.getNodeType() == 1) {
							String nodeName = ele.getNodeName();
							String value = ele.getTextContent().trim();
							if (nodeName.equalsIgnoreCase("protocol")) {
								socketGatewayConfig.setProtocol(SocketProtocol.fromName(value));
							} else if (nodeName.equalsIgnoreCase("host")) {
								socketGatewayConfig.setHost(value);
							} else if (nodeName.equalsIgnoreCase("port")) {
								socketGatewayConfig.setPort(Integer.valueOf(value));
							} else if (nodeName.equalsIgnoreCase("deserializer")) {
								socketGatewayConfig.setDeserializerClassName(value);
							} else if (nodeName.equalsIgnoreCase("serializer")) {
								socketGatewayConfig.setSerializerClassName(value);
							} else if (nodeName.equalsIgnoreCase("name")) {
								socketGatewayConfig.setName(value);
							} else if (nodeName.equalsIgnoreCase("workerpool")) {
								socketGatewayConfig.setWorkerPoolConfig(readWorkerPoolConfig(ele));
							} else if (nodeName.equalsIgnoreCase("uselengthprepender")
									|| nodeName.equalsIgnoreCase("usinglengthprepender")
									|| nodeName.equalsIgnoreCase("prependlength")) {
								socketGatewayConfig.setUseLengthPrepender(Boolean.valueOf(value));
							}
						}
						ele = ele.getNextSibling();
					}
					config = socketGatewayConfig;
					break;
				default:
					throw new RuntimeException(type + " gateway doesn't supported now");
				}

				if (config != null) {
					config.setExtensionName(this.extensionName);
					gatewayConfigs.add(config);
				}
			} else {
				getLogger().warn("gateway type not found: {}", item.getNodeName());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void readDataSourceConfigs(Node node) throws XPathExpressionException {
		NodeList list = (NodeList) xPath.compile("*").evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			if (item.getNodeName().equalsIgnoreCase("sql")) {
				SQLDataSourceConfig config = new SQLDataSourceConfig();
				config.setName(((Node) xPath.compile("name").evaluate(item, XPathConstants.NODE)).getTextContent());
				Node variables = (Node) xPath.compile("variables").evaluate(item, XPathConstants.NODE);
				config.setInitParams(PuObject.fromXML(variables));
				if (this.sqlDatasourceConfigs == null) {
					this.sqlDatasourceConfigs = new ArrayList<SQLDataSourceConfig>();
				}
				this.sqlDatasourceConfigs.add(config);
			} else if (item.getNodeName().equalsIgnoreCase("cassandra")) {
				CassandraConfig config = new CassandraConfig();
				Node currNode = item.getFirstChild();
				while (currNode != null) {
					if (currNode.getNodeType() == 1) {
						if (currNode.getNodeName().equalsIgnoreCase("name")) {
							config.setName(currNode.getTextContent().trim());
						} else if (currNode.getNodeName().equalsIgnoreCase("endpoint")) {
							Object obj = EndpointReader.read(currNode);
							if (obj instanceof HostAndPort) {
								config.getEndpoints().add((HostAndPort) obj);
							} else if (obj instanceof Collection<?>) {
								config.getEndpoints().addAll((Collection<? extends HostAndPort>) obj);
							}
						} else if (currNode.getNodeName().equalsIgnoreCase("keyspace")) {
							config.setKeyspace(currNode.getTextContent().trim());
						}
					}
					currNode = currNode.getNextSibling();
				}
				if (this.cassandraConfigs == null) {
					this.cassandraConfigs = new HashSet<>();
				}
				this.cassandraConfigs.add(config);
			} else if (item.getNodeName().equalsIgnoreCase("hazelcast")) {
				HazelcastConfig config = new HazelcastConfig();
				Node curr = item.getFirstChild();
				while (curr != null) {
					if (curr.getNodeType() == 1) {
						String value = curr.getTextContent().trim();
						switch (curr.getNodeName().trim()) {
						case "name":
							config.setName(value);
							break;
						case "config":
						case "configfile":
							config.setConfigFilePath(value);
							break;
						case "member":
						case "ismember":
							config.setMember(Boolean.valueOf(value));
							break;
						default:
							break;
						}
					}
					curr = curr.getNextSibling();
				}
				config.setExtensionName(extensionName);
				if (this.hazelcastConfigs == null) {
					this.hazelcastConfigs = new ArrayList<>();
				}
				this.hazelcastConfigs.add(config);
			} else if (item.getNodeName().equalsIgnoreCase("redis")) {
				RedisConfig config = new RedisConfig();
				config.setName(((Node) xPath.compile("name").evaluate(item, XPathConstants.NODE)).getTextContent());
				try {
					config.setRedisType(
							((Node) xPath.compile("type").evaluate(item, XPathConstants.NODE)).getTextContent());
				} catch (Exception ex) {
					// default is false
				}
				try {
					config.setMasterName(
							((Node) xPath.compile("mastername").evaluate(item, XPathConstants.NODE)).getTextContent());
				} catch (Exception ex) {
					// do nothing
				}
				NodeList endpoints = (NodeList) xPath.compile("endpoint/entry").evaluate(item, XPathConstants.NODESET);
				for (int j = 0; j < endpoints.getLength(); j++) {
					Node endpointNode = endpoints.item(j);
					String host = null;
					int port = -1;
					boolean isMaster = false;
					try {
						host = ((Node) xPath.compile("host").evaluate(endpointNode, XPathConstants.NODE))
								.getTextContent();
					} catch (Exception ex) {
						getLogger().warn("host config is invalid : " + endpointNode.getTextContent(), ex);
					}
					try {
						port = Integer
								.valueOf(((Node) xPath.compile("port").evaluate(endpointNode, XPathConstants.NODE))
										.getTextContent());
					} catch (Exception ex) {
						getLogger().warn("port config is invalid : " + endpointNode.getTextContent(), ex);
					}
					try {
						isMaster = Boolean
								.valueOf(((Node) xPath.compile("master").evaluate(endpointNode, XPathConstants.NODE))
										.getTextContent());
					} catch (Exception ex) {
						// getLogger().warn("master config is invalid : " +
						// endpointNode.getTextContent(), ex);
					}
					if (host != null && port > 0) {
						HostAndPort endpoint = new HostAndPort(host, port);
						endpoint.setMaster(isMaster);
						config.addEndpoint(endpoint);
					}
				}
				try {
					config.setTimeout(Integer.valueOf(
							((Node) xPath.compile("timeout").evaluate(item, XPathConstants.NODE)).getTextContent()));
				} catch (Exception ex) {
				}
				try {
					config.setPoolSize(Integer.valueOf(
							((Node) xPath.compile("poolsize").evaluate(item, XPathConstants.NODE)).getTextContent()));
				} catch (Exception ex) {
				}
				config.setExtensionName(extensionName);
				if (this.redisConfigs == null) {
					this.redisConfigs = new ArrayList<>();
				}
				this.redisConfigs.add(config);
			} else if (item.getNodeName().equalsIgnoreCase("mongodb")) {
				MongoDBConfig config = new MongoDBConfig();
				config.setName(((Node) xPath.compile("name").evaluate(item, XPathConstants.NODE)).getTextContent());
				NodeList endpoints = (NodeList) xPath.compile("endpoint/entry").evaluate(item, XPathConstants.NODESET);
				for (int j = 0; j < endpoints.getLength(); j++) {
					Node endpointNode = endpoints.item(j);
					String host = null;
					int port = -1;
					try {
						host = ((Node) xPath.compile("host").evaluate(endpointNode, XPathConstants.NODE))
								.getTextContent();
					} catch (Exception ex) {
						getLogger().warn("host config is invalid : " + endpointNode.getTextContent(), ex);
					}
					try {
						port = Integer
								.valueOf(((Node) xPath.compile("port").evaluate(endpointNode, XPathConstants.NODE))
										.getTextContent());
					} catch (Exception ex) {
						getLogger().warn("port config is invalid : " + endpointNode.getTextContent(), ex);
					}
					if (host != null && port > 0) {
						config.addEndpoint(host, port);
					}
				}
				NodeList credentials = (NodeList) xPath.compile("credentials/entry").evaluate(item,
						XPathConstants.NODESET);
				for (int j = 0; j < credentials.getLength(); j++) {
					Node entry = credentials.item(j);
					try {
						MongoDBCredentialConfig credentialConfig = new MongoDBCredentialConfig();
						credentialConfig
								.setUserName(((Node) xPath.compile("username").evaluate(entry, XPathConstants.NODE))
										.getTextContent());
						credentialConfig
								.setPassword(((Node) xPath.compile("password").evaluate(entry, XPathConstants.NODE))
										.getTextContent());
						credentialConfig.setAuthDB(
								((Node) xPath.compile("authdb").evaluate(entry, XPathConstants.NODE)).getTextContent());
						config.addCredentialConfig(credentialConfig);
					} catch (Exception ex) {
						getLogger().warn("credential config is invalid : " + entry.getTextContent(), ex);
					}
				}
				if (this.mongoDBConfigs == null) {
					this.mongoDBConfigs = new ArrayList<MongoDBConfig>();
				}
				this.mongoDBConfigs.add(config);
			} else {
				getLogger().warn("datasource type is not supported: " + item.getNodeName());
			}
		}
	}

	private void readLifeCycleConfigs(Node node) throws XPathExpressionException {
		// read startup config
		this.lifeCycleConfigs = new ArrayList<LifeCycleConfig>();
		if (node == null) {
			return;
		}
		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeType() == 1) {
				LifeCycleConfig config = null;
				if (item.getNodeName().equalsIgnoreCase("handler")) {
					MessageHandlerConfig messageHandlerConfig = new MessageHandlerConfig();
					Object gatewaysObj = xPath.compile("bind/gateway").evaluate(item, XPathConstants.NODESET);
					if (gatewaysObj != null) {
						NodeList gateways = (NodeList) gatewaysObj;
						for (int j = 0; j < gateways.getLength(); j++) {
							messageHandlerConfig.getBindingGateways().add(gateways.item(j).getTextContent().trim());
						}
					}
					config = messageHandlerConfig;
				} else if (item.getNodeName().equalsIgnoreCase("managedobject")) {
					config = new ManagedObjectConfig();
				} else if (item.getNodeName().equalsIgnoreCase("entry")) {
					config = new LifeCycleConfig();
				}

				if (config != null) {
					String name = ((Node) xPath.compile("name").evaluate(item, XPathConstants.NODE)).getTextContent();
					String handleClass = ((Node) xPath.compile("handle").evaluate(item, XPathConstants.NODE))
							.getTextContent();
					config.setName(name);
					config.setExtensionName(extensionName);
					config.setHandleClass(handleClass);

					Object variableObj = xPath.compile("variables").evaluate(item, XPathConstants.NODE);
					if (variableObj != null) {
						config.setInitParams(PuObject.fromXML((Node) variableObj));
					}

					this.lifeCycleConfigs.add(config);
				} else {
					getLogger().warn("lifecycle definition cannot be recognized: " + item);
				}
			}
			item = item.getNextSibling();
		}
	}

	private void readMonitorAgentConfigs(Node node) throws Exception {
		this.monitorAgentConfigs = new ArrayList<>();
	}

	private void readProducerConfigs(Node node) throws XPathExpressionException {
		this.producerConfigs = new ArrayList<>();
		if (node == null) {
			return;
		}
		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeType() == 1) {
				GatewayType gatewayType = GatewayType.fromName(item.getNodeName());
				MessageProducerConfig config = null;
				Node ele = item.getFirstChild();
				switch (gatewayType) {
				case RABBITMQ:
					RabbitMQProducerConfig rabbitMQProducerConfig = new RabbitMQProducerConfig();
					while (ele != null) {
						if (ele.getNodeType() == 1) {
							String nodeName = ele.getNodeName();
							String value = ele.getTextContent().trim();
							if (nodeName.equalsIgnoreCase("server")) {
								rabbitMQProducerConfig.setConnectionName(value);
							} else if (nodeName.equalsIgnoreCase("timeout")) {
								rabbitMQProducerConfig.setTimeout(Integer.valueOf(value));
							} else if (nodeName.equalsIgnoreCase("queue")) {
								rabbitMQProducerConfig.setQueueConfig(readRabbitMQQueueConfig(ele));
							}
						}
						ele = ele.getNextSibling();
					}
					config = rabbitMQProducerConfig;
					break;
				case HTTP:
					HttpMessageProducerConfig httpMessageProducerConfig = new HttpMessageProducerConfig();
					while (ele != null) {
						if (ele.getNodeType() == 1) {
							String nodeName = ele.getNodeName();
							String value = ele.getTextContent().trim();
							if (nodeName.equalsIgnoreCase("endpoint")) {
								httpMessageProducerConfig.setEndpoint(value);
							} else if (nodeName.equalsIgnoreCase("method")) {
								httpMessageProducerConfig.setHttpMethod(HttpMethod.fromName(value));
							} else if (nodeName.equalsIgnoreCase("async")) {
								httpMessageProducerConfig.setAsync(Boolean.valueOf(value));
							} else if (nodeName.equalsIgnoreCase("usemultipath")
									|| nodeName.equalsIgnoreCase("usingmultipath")) {
								httpMessageProducerConfig.setUsingMultipath(Boolean.valueOf(value));
							}
						}
						ele = ele.getNextSibling();
					}
					config = httpMessageProducerConfig;
					break;
				case SOCKET:
				default:
					throw new UnsupportedTypeException();
				}

				if (config != null) {
					if (config.getGatewayType() == null) {
						config.setGatewayType(gatewayType);
					}
					config.setName(((Node) xPath.compile("name").evaluate(item, XPathConstants.NODE)).getTextContent());
					config.setExtensionName(this.extensionName);
					this.producerConfigs.add(config);
				}
			}
			item = item.getNextSibling();
		}
	}

	public String getExtensionName() {
		return extensionName;
	}

	public List<GatewayConfig> getGatewayConfigs() {
		return this.gatewayConfigs;
	}

	public List<SQLDataSourceConfig> getSQLDataSourceConfig() {
		return this.sqlDatasourceConfigs;
	}

	public List<HazelcastConfig> getHazelcastConfigs() {
		return hazelcastConfigs;
	}

	public List<RedisConfig> getRedisConfigs() {
		return this.redisConfigs;
	}

	public List<LifeCycleConfig> getLifeCycleConfigs() {
		return this.lifeCycleConfigs;
	}

	public List<MongoDBConfig> getMongoDBConfigs() {
		return this.mongoDBConfigs;
	}

	public List<ServerWrapperConfig> getServerWrapperConfigs() {
		return this.serverWrapperConfigs;
	}

	public Collection<? extends MonitorAgentConfig> getMonitorAgentConfigs() {
		return this.monitorAgentConfigs;
	}

	public Collection<? extends MessageProducerConfig> getProducerConfigs() {
		return this.producerConfigs;
	}

	public Collection<? extends CassandraDatasourceConfig> getCassandraConfigs() {
		return this.cassandraConfigs;
	}

}
