// Package config provides a functionality to read from yml config file
// and provides values for each key in the file
// the package is splitted into multiple for simplicity and readability
// RabbitMQ config
package config

// RabbitMQ struct
type RabbitMQConfiguration struct {
	Connection string // connection string to RabbitMQ
	QueueName  string // RabbitMQ queue
}

// Define all interfaces for this struct
type IRabbitMQConfiguration interface {
	GetConnection() string
	GetQueueName() string
}

// Implementation
func (r RabbitMQConfiguration) GetConnection() string {
	return r.Connection
}

func (r RabbitMQConfiguration) GetQueueName() string {
	return r.QueueName
}
