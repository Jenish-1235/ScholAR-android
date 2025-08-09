package com.android.scholar.utils

import com.android.scholar.model.LearningResponse

object SampleData {
    val sampleLearningResponse = LearningResponse(
        type = "learning_response",
        explanation = "Newton's Second Law states that the force acting on an object is equal to its mass multiplied by its acceleration (F = ma). This means that heavier objects require more force to accelerate, and the faster you want an object to speed up, the more force you need. This fundamental principle explains why a car needs more power to accelerate quickly or why it's harder to push a heavy box than a light one.",
        practiceQuestions = listOf(
            "What is Newton's Second Law and how is it expressed mathematically?",
            "How does mass affect the force needed to accelerate an object?",
            "If an object has zero acceleration, what can we say about the net force acting on it?",
            "How do you calculate acceleration if you know force and mass?",
            "Give an example where Newton's Second Law is applied in daily life."
        ),
        additionalUrls = listOf(
            "https://www.khanacademy.org/science/physics/forces-newtons-laws/newtons-laws-of-motion/a/what-is-newtons-second-law",
            "https://en.wikipedia.org/wiki/Newton%27s_laws_of_motion#Second_law",
            "https://www.physicsclassroom.com/class/newtlaws/Lesson-2/Newton-s-Second-Law"
        ),
        ttsUrl = "/static/tts/tts_1692542400.mp3"
    )
    
    fun getSampleResponseJson(): String {
        return """
        {
          "type": "learning_response",
          "explanation": "Newton's Second Law states that the force acting on an object is equal to its mass multiplied by its acceleration (F = ma). This means that heavier objects require more force to accelerate, and the faster you want an object to speed up, the more force you need.",
          "practice_questions": [
            "What is Newton's Second Law and how is it expressed mathematically?",
            "How does mass affect the force needed to accelerate an object?",
            "If an object has zero acceleration, what can we say about the net force acting on it?",
            "How do you calculate acceleration if you know force and mass?",
            "Give an example where Newton's Second Law is applied in daily life."
          ],
          "additional_urls": [
            "https://www.khanacademy.org/science/physics/forces-newtons-laws/newtons-laws-of-motion/a/what-is-newtons-second-law",
            "https://en.wikipedia.org/wiki/Newton%27s_laws_of_motion#Second_law",
            "https://www.physicsclassroom.com/class/newtlaws/Lesson-2/Newton-s-Second-Law"
          ],
          "tts_url": "/static/tts/tts_1692542400.mp3"
        }
        """.trimIndent()
    }
}
