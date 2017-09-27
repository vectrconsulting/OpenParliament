package consulting.vectr.model

case class ParliamentaryQuestion(
    author: String, 
    party: String, 
    status: String,
    topicNL: Option[String],
    subTopicNL: Set[String],
    department: String,
    questionId: String)
    
case class ParliamentaryQuestionSummary(
    author: String, 
    party: String, 
    topicNL: String,
    departmentNL: String,
    questionCount: Int)

case class MemberOfParliament(name: String,surname: String)
  
