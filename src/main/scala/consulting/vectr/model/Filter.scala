package consulting.vectr.model

case class Filter(
                   id: Int,
                   public: Boolean,
                   count: Int,
                   question: String,
                   entities: List[FilterEntity]
                 )
